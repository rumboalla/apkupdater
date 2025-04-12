import * as doc from "prettier/doc";

const {
  dedentToRoot,
  group,
  hardline,
  indent,
  join,
  line,
  literalline,
  softline
} = doc.builders;

// Get the start and end element tags from the current node on the tree
function getElementTags(path, opts, print) {
  const node = path.getValue();
  const { OPEN, Name, attribute, START_CLOSE, SLASH_OPEN, END_NAME, END } =
    node;

  const parts = [OPEN, Name];

  if (attribute.length > 0) {
    parts.push(indent([line, join(line, path.map(print, "attribute"))]));
  }

  if (!opts.bracketSameLine) {
    parts.push(softline);
  }

  return {
    openTag: group([...parts, START_CLOSE]),
    closeTag: group([SLASH_OPEN, END_NAME, END])
  };
}

// Returns the value of the type tag if there is one, otherwise returns null.
function getTagType(attributes) {
  for (const attribute of attributes) {
    if (attribute.Name === "type") {
      const value = attribute.STRING;

      if (value.startsWith('"text/') && value.endsWith('"')) {
        return value.slice(6, -1);
      }
    }
  }

  return null;
}

// Get the name of the parser that is represented by the given element node,
// return null if a matching parser cannot be found
function getParser(node, opts) {
  const { Name, attribute } = node;
  let parser = Name.toLowerCase();

  // We don't want to deal with some weird recursive parser situation, so we
  // need to explicitly call out the XML parser here and just return null
  if (parser === "xml") {
    return null;
  }

  // If this is a style tag or a script tag with a text/xxx type then we will
  // use xxx as the name of the parser
  if ((parser === "style" || parser === "script") && attribute.length > 0) {
    parser = getTagType(attribute);
  }

  // If the name of the parser is "javascript", then we're going to switch over
  // to the babel parser.
  if (parser === "javascript") {
    parser = "babel";
  }

  // If there is a plugin that has a parser that matches the name of this
  // element, then we're going to assume that's correct for embedding and go
  // ahead and switch to that parser
  if (
    opts.plugins.some(
      (plugin) =>
        typeof plugin !== "string" &&
        plugin.parsers &&
        Object.prototype.hasOwnProperty.call(plugin.parsers, parser)
    )
  ) {
    return parser;
  }

  return null;
}

// Get the source string that will be passed into the embedded parser from the
// content of the inside of the element node
function getSource(content) {
  return content.chardata
    .map((node) => {
      const { SEA_WS, TEXT } = node;
      const image = SEA_WS || TEXT;

      return {
        offset: node.location.startOffset,
        printed: image
      };
    })
    .sort(({ offset }) => offset)
    .map(({ printed }) => printed)
    .join("");
}

function embed(path, opts) {
  const node = path.getValue();

  // If the node isn't an element node, then skip
  if (node.name !== "element") {
    return;
  }

  // If the name of the node does not correspond to the name of a parser that
  // prettier knows about, then skip
  const parser = getParser(node, opts);
  if (!parser) {
    return;
  }

  // If the node is self-closing, then skip
  if (!node.content) {
    return;
  }

  // If the node does not actually contain content, or it contains any content
  // that is not just plain text, then skip.
  const content = node.content;
  if (
    content.chardata.length === 0 ||
    content.CData.length > 0 ||
    content.Comment.length > 0 ||
    content.element.length > 0 ||
    content.PROCESSING_INSTRUCTION.length > 0 ||
    content.reference.length > 0
  ) {
    return;
  }

  return async function (textToDoc, print) {
    // Get the open and close tags of this element, then return the properly
    // formatted content enclosed within them
    const { openTag, closeTag } = getElementTags(path, opts, print);
    const docNode = await textToDoc(getSource(content), { parser });

    return group([
      openTag,
      literalline,
      dedentToRoot(doc.utils.replaceEndOfLine(docNode)),
      hardline,
      closeTag
    ]);
  };
}

export default embed;
