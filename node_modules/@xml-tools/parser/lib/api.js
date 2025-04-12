const { xmlLexer } = require("./lexer");
const { xmlParser } = require("./parser");

module.exports = {
  parse: function parse(text) {
    const lexResult = xmlLexer.tokenize(text);
    // setting a new input will RESET the parser instance's state.
    xmlParser.input = lexResult.tokens;
    // any top level rule may be used as an entry point
    const cst = xmlParser.document();

    return {
      cst: cst,
      tokenVector: lexResult.tokens,
      lexErrors: lexResult.errors,
      parseErrors: xmlParser.errors,
    };
  },

  BaseXmlCstVisitor: xmlParser.getBaseCstVisitorConstructor(),
};
