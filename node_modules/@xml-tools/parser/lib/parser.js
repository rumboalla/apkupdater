const { CstParser, tokenMatcher } = require("chevrotain");
const { tokensDictionary: t } = require("./lexer");

class Parser extends CstParser {
  constructor() {
    super(t, {
      maxLookahead: 1,
      recoveryEnabled: true,
      nodeLocationTracking: "full",
    });

    this.deletionRecoveryEnabled = true;

    const $ = this;

    $.RULE("document", () => {
      $.OPTION(() => {
        $.SUBRULE($.prolog);
      });

      $.MANY(() => {
        $.SUBRULE($.misc);
      });

      $.OPTION2(() => {
        $.SUBRULE($.docTypeDecl);
      });

      $.MANY2(() => {
        $.SUBRULE2($.misc);
      });

      $.SUBRULE($.element);

      $.MANY3(() => {
        $.SUBRULE3($.misc);
      });
    });

    $.RULE("prolog", () => {
      $.CONSUME(t.XMLDeclOpen);
      $.MANY(() => {
        $.SUBRULE($.attribute);
      });
      $.CONSUME(t.SPECIAL_CLOSE);
    });

    // https://www.w3.org/TR/xml/#NT-doctypedecl
    $.RULE("docTypeDecl", () => {
      $.CONSUME(t.DocType);
      $.CONSUME(t.Name);

      $.OPTION(() => {
        $.SUBRULE($.externalID);
      });

      // The internal subSet part is intentionally not implemented because we do not at this
      // time wish to implement a full DTD Parser as part of this project...
      // https://www.w3.org/TR/xml/#NT-intSubset

      $.CONSUME(t.CLOSE);
    });

    $.RULE("externalID", () => {
      // Using gates to assert the value of the "Name" Identifiers.
      // We could use Categories to model un-reserved keywords, however I am not sure
      // The added complexity is needed at this time...
      $.OR([
        {
          GATE: () => $.LA(1).image === "SYSTEM",
          ALT: () => {
            $.CONSUME2(t.Name, { LABEL: "System" });
            $.CONSUME(t.STRING, { LABEL: "SystemLiteral" });
          },
        },
        {
          GATE: () => $.LA(1).image === "PUBLIC",
          ALT: () => {
            $.CONSUME3(t.Name, { LABEL: "Public" });
            $.CONSUME2(t.STRING, { LABEL: "PubIDLiteral" });
            $.CONSUME3(t.STRING, { LABEL: "SystemLiteral" });
          },
        },
      ]);
    });

    $.RULE("content", () => {
      $.MANY(() => {
        $.OR([
          { ALT: () => $.SUBRULE($.element) },
          { ALT: () => $.SUBRULE($.chardata) },
          { ALT: () => $.SUBRULE($.reference) },
          { ALT: () => $.CONSUME(t.CData) },
          { ALT: () => $.CONSUME(t.PROCESSING_INSTRUCTION) },
          { ALT: () => $.CONSUME(t.Comment) },
        ]);
      });
    });

    $.RULE("element", () => {
      $.CONSUME(t.OPEN);
      try {
        this.deletionRecoveryEnabled = false;
        // disabling single token deletion here
        // because `<
        //            </note>`
        // will be parsed as: `<note>`
        // and the next element will be lost
        $.CONSUME(t.Name);
      } finally {
        this.deletionRecoveryEnabled = true;
      }
      $.MANY(() => {
        $.SUBRULE($.attribute);
      });

      $.OR([
        {
          ALT: () => {
            $.CONSUME(t.CLOSE, { LABEL: "START_CLOSE" });
            $.SUBRULE($.content);
            $.CONSUME(t.SLASH_OPEN);
            $.CONSUME2(t.Name, { LABEL: "END_NAME" });
            $.CONSUME2(t.CLOSE, { LABEL: "END" });
          },
        },
        {
          ALT: () => {
            $.CONSUME(t.SLASH_CLOSE);
          },
        },
      ]);
    });

    $.RULE("reference", () => {
      $.OR([
        { ALT: () => $.CONSUME(t.EntityRef) },
        { ALT: () => $.CONSUME(t.CharRef) },
      ]);
    });

    $.RULE("attribute", () => {
      $.CONSUME(t.Name);
      try {
        this.deletionRecoveryEnabled = false;
        // disabling single token deletion here
        // because `attrib1 attrib2="666`
        // will be parsed as: `attrib1="666`
        $.CONSUME(t.EQUALS);
        // disabling single token deletion here
        // to avoid new elementName being
        $.CONSUME(t.STRING);
      } finally {
        this.deletionRecoveryEnabled = true;
      }
    });

    $.RULE("chardata", () => {
      $.OR([
        { ALT: () => $.CONSUME(t.TEXT) },
        { ALT: () => $.CONSUME(t.SEA_WS) },
      ]);
    });

    $.RULE("misc", () => {
      $.OR([
        { ALT: () => $.CONSUME(t.Comment) },
        { ALT: () => $.CONSUME(t.PROCESSING_INSTRUCTION) },
        { ALT: () => $.CONSUME(t.SEA_WS) },
      ]);
    });

    this.performSelfAnalysis();
  }

  canRecoverWithSingleTokenDeletion(expectedTokType) {
    if (this.deletionRecoveryEnabled === false) {
      return false;
    }
    return super.canRecoverWithSingleTokenDeletion(expectedTokType);
  }

  // TODO: provide this fix upstream to chevrotain
  // https://github.com/SAP/chevrotain/issues/1055
  /* istanbul ignore next - should be tested as part of Chevrotain */
  findReSyncTokenType() {
    const allPossibleReSyncTokTypes = this.flattenFollowSet();
    // this loop will always terminate as EOF is always in the follow stack and also always (virtually) in the input
    let nextToken = this.LA(1);
    let k = 2;
    /* eslint-disable-next-line no-constant-condition -- see above comment */
    while (true) {
      const foundMatch = allPossibleReSyncTokTypes.find((resyncTokType) => {
        const canMatch = tokenMatcher(nextToken, resyncTokType);
        return canMatch;
      });
      if (foundMatch !== undefined) {
        return foundMatch;
      }
      nextToken = this.LA(k);
      k++;
    }
  }
}

// Re-use the same parser instance
const xmlParser = new Parser();

module.exports = {
  xmlParser,
};
