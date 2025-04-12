import * as doc from "prettier/doc";
import embed from "./embed.js";

const { fill, group, hardline, indent, join, line, literalline, softline } =
  doc.builders;

const ignoreStartComment = "<!-- prettier-ignore-start -->";
const ignoreEndComment = "<!-- prettier-ignore-end -->";

function hasIgnoreRanges(comments) {
  if (comments.length === 0) {
    return false;
  }

  comments.sort((left, right) => left.startOffset - right.startOffset);

  let startFound = false;
  for (let idx = 0; idx < comments.length; idx += 1) {
    if (comments[idx].image === ignoreStartComment) {
      startFound = true;
    } else if (startFound && comments[idx].image === ignoreEndComment) {
      return true;
    }
  }

  return false;
}

function isWhitespaceIgnorable(opts, name, attributes, content) {
  // If the whitespace sensitivity setting is "strict", then we can't ignore the
  // whitespace.
  if (opts.xmlWhitespaceSensitivity === "strict") {
    return false;
  }

  // If we have an xsl:text element, then we cannot ignore the whitespace.
  if (name === "xsl:text") {
    return false;
  }

  // If there is an xml:space attribute set to "preserve", then we can't ignore
  // the whitespace.
  if (
    attributes.some(
      (attribute) =>
        attribute &&
        attribute.Name === "xml:space" &&
        attribute.STRING.slice(1, -1) === "preserve"
    )
  ) {
    return false;
  }

  // If there are character data nodes in the content, then we can't ignore the
  // whitespace.
  if (content.CData.length > 0) {
    return false;
  }

  // If there are comments in the content and the comments are ignore ranges,
  // then we can't ignore the whitespace.
  if (hasIgnoreRanges(content.Comment)) {
    return false;
  }

  // Otherwise we can.
  return true;
}

function printIToken(path) {
  const node = path.getValue();

  return {
    offset: node.startOffset,
    startLine: node.startLine,
    endLine: node.endLine,
    printed: node.image
  };
}

function printAttribute(path, opts, print) {
  const { Name, EQUALS, STRING } = path.getValue();

  let attributeValue;
  if (opts.xmlQuoteAttributes === "double") {
    const content = STRING.slice(1, -1).replaceAll('"', "&quot;");
    attributeValue = `"${content}"`;
  } else if (opts.xmlQuoteAttributes === "single") {
    const content = STRING.slice(1, -1).replaceAll("'", "&apos;");
    attributeValue = `'${content}'`;
  } else {
    // preserve
    attributeValue = STRING;
  }

  return [Name, EQUALS, attributeValue];
}

function printCharData(path, opts, print) {
  const { SEA_WS, TEXT } = path.getValue();
  const image = SEA_WS || TEXT;

  return image
    .split(/(\n)/g)
    .map((value, index) => (index % 2 === 0 ? value : literalline));
}

function printContentFragments(path, print) {
  return [
    ...path.map(printIToken, "CData"),
    ...path.map(printIToken, "Comment"),
    ...path.map(
      (charDataPath) => ({
        offset: charDataPath.getValue().location.startOffset,
        printed: print(charDataPath)
      }),
      "chardata"
    ),
    ...path.map(
      (elementPath) => ({
        offset: elementPath.getValue().location.startOffset,
        printed: print(elementPath)
      }),
      "element"
    ),
    ...path.map(printIToken, "PROCESSING_INSTRUCTION"),
    ...path.map((referencePath) => {
      const referenceNode = referencePath.getValue();

      return {
        offset: referenceNode.location.startOffset,
        printed: print(referencePath)
      };
    }, "reference")
  ];
}

function printContent(path, opts, print) {
  let fragments = printContentFragments(path, print);
  const { Comment } = path.getValue();

  if (hasIgnoreRanges(Comment)) {
    Comment.sort((left, right) => left.startOffset - right.startOffset);

    const ignoreRanges = [];
    let ignoreStart = null;

    // Build up a list of ignored ranges from the original text based on
    // the special prettier-ignore-* comments
    Comment.forEach((comment) => {
      if (comment.image === ignoreStartComment) {
        ignoreStart = comment;
      } else if (ignoreStart && comment.image === ignoreEndComment) {
        ignoreRanges.push({
          start: ignoreStart.startOffset,
          end: comment.endOffset
        });

        ignoreStart = null;
      }
    });

    // Filter the printed children to only include the ones that are
    // outside of each of the ignored ranges
    fragments = fragments.filter((fragment) =>
      ignoreRanges.every(
        ({ start, end }) => fragment.offset < start || fragment.offset > end
      )
    );

    // Push each of the ignored ranges into the child list as its own
    // element so that the original content is still included
    ignoreRanges.forEach(({ start, end }) => {
      const content = opts.originalText.slice(start, end + 1);

      fragments.push({
        offset: start,
        printed: doc.utils.replaceEndOfLine(content)
      });
    });
  }

  fragments.sort((left, right) => left.offset - right.offset);
  return group(fragments.map(({ printed }) => printed));
}

function printDocTypeDecl(path, opts, print) {
  const { DocType, Name, externalID, CLOSE } = path.getValue();
  const parts = [DocType, " ", Name];

  if (externalID) {
    parts.push(" ", path.call(print, "externalID"));
  }

  return group([...parts, CLOSE]);
}

function printDocument(path, opts, print) {
  const { docTypeDecl, element, misc, prolog } = path.getValue();
  const fragments = [];

  if (docTypeDecl) {
    fragments.push({
      offset: docTypeDecl.location.startOffset,
      printed: path.call(print, "docTypeDecl")
    });
  }

  if (prolog) {
    fragments.push({
      offset: prolog.location.startOffset,
      printed: path.call(print, "prolog")
    });
  }

  path.each((miscPath) => {
    const misc = miscPath.getValue();

    fragments.push({
      offset: misc.location.startOffset,
      printed: print(miscPath)
    });
  }, "misc");

  if (element) {
    fragments.push({
      offset: element.location.startOffset,
      printed: path.call(print, "element")
    });
  }

  fragments.sort((left, right) => left.offset - right.offset);

  return [
    join(
      hardline,
      fragments.map(({ printed }) => printed)
    ),
    hardline
  ];
}

function printCharDataPreserve(path, print) {
  let prevLocation;
  const response = [];

  path.each((charDataPath) => {
    const chardata = charDataPath.getValue();
    const location = chardata.location;
    const content = print(charDataPath);

    if (
      prevLocation &&
      location.startColumn &&
      prevLocation.endColumn &&
      location.startLine === prevLocation.endLine &&
      location.startColumn === prevLocation.endColumn + 1
    ) {
      // continuation of previous fragment
      const prevFragment = response[response.length - 1];
      prevFragment.endLine = location.endLine;
      prevFragment.printed = group([prevFragment.printed, content]);
    } else {
      response.push({
        offset: location.startOffset,
        startLine: location.startLine,
        endLine: location.endLine,
        printed: content,
        whitespace: true
      });
    }
    prevLocation = location;
  }, "chardata");

  return response;
}

function printCharDataIgnore(path) {
  const response = [];

  path.each((charDataPath) => {
    const chardata = charDataPath.getValue();
    if (!chardata.TEXT) {
      return;
    }

    const content = chardata.TEXT.replaceAll(/^[\t\n\r\s]+|[\t\n\r\s]+$/g, "");
    const printed = group(
      content.split(/(\n)/g).map((value) => {
        if (value === "\n") {
          return literalline;
        }

        return fill(
          value
            .split(/\b( +)\b/g)
            .map((segment, index) => (index % 2 === 0 ? segment : line))
        );
      })
    );

    const location = chardata.location;
    response.push({
      offset: location.startOffset,
      startLine: location.startLine,
      endLine: location.endLine,
      printed
    });
  }, "chardata");

  return response;
}

function printElementFragments(path, opts, print) {
  const children = path.getValue();
  let response = [];

  response = response.concat(path.map(printIToken, "Comment"));

  if (children.chardata.length > 0) {
    if (
      children.chardata.some((chardata) => !!chardata.TEXT) &&
      opts.xmlWhitespaceSensitivity === "preserve"
    ) {
      response = response.concat(printCharDataPreserve(path, print));
    } else {
      response = response.concat(printCharDataIgnore(path, print));
    }
  }

  response = response.concat(
    path.map((elementPath) => {
      const location = elementPath.getValue().location;

      return {
        offset: location.startOffset,
        startLine: location.startLine,
        endLine: location.endLine,
        printed: print(elementPath)
      };
    }, "element")
  );

  response = response.concat(path.map(printIToken, "PROCESSING_INSTRUCTION"));

  response = response.concat(
    path.map((referencePath) => {
      const referenceNode = referencePath.getValue();

      return {
        type: "reference",
        offset: referenceNode.location.startOffset,
        startLine: referenceNode.location.startLine,
        endLine: referenceNode.location.endLine,
        printed: print(referencePath)
      };
    }, "reference")
  );

  return response;
}

function printElement(path, opts, print) {
  const {
    OPEN,
    Name,
    attribute,
    START_CLOSE,
    content,
    SLASH_OPEN,
    END_NAME,
    END,
    SLASH_CLOSE
  } = path.getValue();

  const parts = [OPEN, Name];

  if (attribute.length > 0) {
    const attributes = path.map(
      (attributePath) => ({
        node: attributePath.getValue(),
        printed: print(attributePath)
      }),
      "attribute"
    );

    if (opts.xmlSortAttributesByKey) {
      attributes.sort((left, right) => {
        const leftAttr = left.node.Name;
        const rightAttr = right.node.Name;

        // Check if the attributes are xmlns.
        if (leftAttr === "xmlns") return -1;
        if (rightAttr === "xmlns") return 1;

        // Check if they are both in namespaces.
        if (leftAttr.includes(":") && rightAttr.includes(":")) {
          const [leftNS, leftKey] = leftAttr.split(":");
          const [rightNS, rightKey] = rightAttr.split(":");

          // If namespaces are equal, compare keys
          if (leftNS === rightNS) return leftKey.localeCompare(rightKey);

          // Handle the 1 but not both being an xmlns
          if (leftNS === "xmlns") return -1;
          if (rightNS === "xmlns") return 1;

          return leftNS.localeCompare(rightNS);
        }

        // Check if the attributes have namespaces.
        if (leftAttr.includes(":")) return -1;
        if (rightAttr.includes(":")) return 1;

        return leftAttr.localeCompare(rightAttr);
      });
    }

    const separator = opts.singleAttributePerLine ? hardline : line;
    parts.push(
      indent([
        line,
        join(
          separator,
          attributes.map(({ printed }) => printed)
        )
      ])
    );
  }

  // Determine the value that will go between the <, name, and attributes
  // of an element and the /> of an element.
  let space;
  if (opts.bracketSameLine) {
    space = opts.xmlSelfClosingSpace ? " " : "";
  } else {
    space = opts.xmlSelfClosingSpace ? line : softline;
  }

  if (SLASH_CLOSE) {
    return group([...parts, space, SLASH_CLOSE]);
  }

  if (
    content.chardata.length === 0 &&
    content.CData.length === 0 &&
    content.Comment.length === 0 &&
    content.element.length === 0 &&
    content.PROCESSING_INSTRUCTION.length === 0 &&
    content.reference.length === 0
  ) {
    return group([...parts, space, "/>"]);
  }

  const openTag = group([
    ...parts,
    opts.bracketSameLine ? "" : softline,
    START_CLOSE
  ]);

  const closeTag = group([SLASH_OPEN, END_NAME, END]);

  if (isWhitespaceIgnorable(opts, Name, attribute, content)) {
    const fragments = path.call(
      (childrenPath) => printElementFragments(childrenPath, opts, print),
      "content"
    );

    fragments.sort((left, right) => left.offset - right.offset);

    if (
      opts.xmlWhitespaceSensitivity === "preserve" &&
      fragments.some(({ whitespace }) => whitespace)
    ) {
      return group([
        openTag,
        fragments.map(({ printed }) => printed),
        closeTag
      ]);
    }

    if (fragments.length === 0) {
      return group([...parts, space, "/>"]);
    }

    // If the only content of this tag is chardata, then use a softline so
    // that we won't necessarily break (to allow <foo>bar</foo>).
    if (
      fragments.length === 1 &&
      content.chardata.filter((chardata) => chardata.TEXT).length === 1
    ) {
      return group([
        openTag,
        indent([softline, fragments[0].printed]),
        softline,
        closeTag
      ]);
    }

    let delimiter = hardline;

    // If the only content is both chardata and references, then use a softline
    // so that we won't necessarily break.
    if (
      fragments.length ===
      content.chardata.filter((chardata) => chardata.TEXT).length +
        content.reference.length
    ) {
      delimiter = " ";
    }

    const docs = [hardline];
    let lastLine = fragments[0].startLine;

    fragments.forEach((node, index) => {
      if (index !== 0) {
        if (node.startLine - lastLine >= 2) {
          docs.push(hardline, hardline);
        } else {
          docs.push(delimiter);
        }
      }

      docs.push(node.printed);
      lastLine = node.endLine;
    });

    return group([openTag, indent(docs), hardline, closeTag]);
  }

  return group([openTag, indent(path.call(print, "content")), closeTag]);
}

function printExternalID(path, opts, print) {
  const { Public, PubIDLiteral, System, SystemLiteral } = path.getValue();

  if (System) {
    return group([System, indent([line, SystemLiteral])]);
  }

  return group([
    group([Public, indent([line, PubIDLiteral])]),
    indent([line, SystemLiteral])
  ]);
}

function printMisc(path, opts, print) {
  const { Comment, PROCESSING_INSTRUCTION, SEA_WS } = path.getValue();

  return Comment || PROCESSING_INSTRUCTION || SEA_WS;
}

function printProlog(path, opts, print) {
  const { XMLDeclOpen, attribute, SPECIAL_CLOSE } = path.getValue();
  const parts = [XMLDeclOpen];

  if (attribute) {
    parts.push(indent([softline, join(line, path.map(print, "attribute"))]));
  }

  return group([
    ...parts,
    opts.xmlSelfClosingSpace ? line : softline,
    SPECIAL_CLOSE
  ]);
}

function printReference(path, opts, print) {
  const { CharRef, EntityRef } = path.getValue();

  return CharRef || EntityRef;
}

const printer = {
  getVisitorKeys(node, nonTraversableKeys) {
    return Object.keys(node).filter(
      (key) => key !== "location" && key !== "tokenType"
    );
  },
  embed,
  print(path, opts, print) {
    const node = path.getValue();

    switch (node.name) {
      case "attribute":
        return printAttribute(path, opts, print);
      case "chardata":
        return printCharData(path, opts, print);
      case "content":
        return printContent(path, opts, print);
      case "docTypeDecl":
        return printDocTypeDecl(path, opts, print);
      case "document":
        return printDocument(path, opts, print);
      case "element":
        return printElement(path, opts, print);
      case "externalID":
        return printExternalID(path, opts, print);
      case "misc":
        return printMisc(path, opts, print);
      case "prolog":
        return printProlog(path, opts, print);
      case "reference":
        return printReference(path, opts, print);
      default:
        throw new Error(`Unknown node type: ${node.name}`);
    }
  }
};

export default printer;
