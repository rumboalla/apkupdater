import {
  CstNode,
  ICstVisitor,
  ILexingError,
  IRecognitionException,
  IToken,
} from "chevrotain";

export function parse(
  text: string,
  startProduction?: string
): {
  cst: CstNode;
  tokenVector: IToken[];
  // TODO: decouple from Chevrotain APIs?
  lexErrors: ILexingError[];
  parseErrors: IRecognitionException[];
};

export const BaseXmlCstVisitor: XmlCstVisitorConstructor<any, any>;
export const BaseXmlCstVisitorWithDefaults: XmlCstVisitorWithDefaultsConstructor<
  any,
  any
>;

export abstract class XmlCstVisitor<IN, OUT> implements ICstVisitor<IN, OUT> {
  // No need to implement these two methods
  // Generic Visit method implemented by the Chevrotain Library
  visit(cstNode: CstNode | CstNode[], param?: IN): OUT;
  validateVisitor(): void;

  document(ctx: DocumentCtx, param?: IN): OUT;
  prolog(ctx: PrologCtx, param?: IN): OUT;
  docTypeDecl(ctx: DocTypeDeclCtx, param?: IN): OUT;
  externalID(ctx: ExternalIDCtx, param?: IN): OUT;
  content(ctx: ContentCtx, param?: IN): OUT;
  element(ctx: ElementCtx, param?: IN): OUT;
  reference(ctx: ReferenceCtx, param?: IN): OUT;
  attribute(ctx: AttributeCtx, param?: IN): OUT;
  chardata(ctx: ChardataCtx, param?: IN): OUT;
  misc(ctx: MiscCtx, param?: IN): OUT;
}

interface XmlCstVisitorConstructor<IN, OUT> {
  new (): XmlCstVisitor<IN, OUT>;
}

export abstract class XmlCstVisitorWithDefaults<IN, OUT>
  implements ICstVisitor<IN, OUT> {
  // No need to implement these two methods
  // Generic Visit method implemented by the Chevrotain Library
  visit(cstNode: CstNode | CstNode[], param?: IN): OUT;
  validateVisitor(): void;

  document(ctx: DocumentCtx, param?: IN): OUT;
  prolog(ctx: PrologCtx, param?: IN): OUT;
  docTypeDecl(ctx: DocTypeDeclCtx, param?: IN): OUT;
  externalID(ctx: ExternalIDCtx, param?: IN): OUT;
  content(ctx: ContentCtx, param?: IN): OUT;
  element(ctx: ElementCtx, param?: IN): OUT;
  reference(ctx: ReferenceCtx, param?: IN): OUT;
  attribute(ctx: AttributeCtx, param?: IN): OUT;
  chardata(ctx: ChardataCtx, param?: IN): OUT;
  misc(ctx: MiscCtx, param?: IN): OUT;
}

interface XmlCstVisitorWithDefaultsConstructor<IN, OUT> {
  new (): XmlCstVisitorWithDefaults<IN, OUT>;
}

export interface DocumentCstNode extends CstNode {
  name: "document";
  children: DocumentCtx;
}
export type DocumentCtx = {
  prolog: PrologCstNode[];
  docTypeDecl: DocTypeDeclNode[];
  misc: MiscCstNode[];
  element: ElementCstNode[];
};

export interface PrologCstNode extends CstNode {
  name: "prolog";
  children: PrologCtx;
}

export type PrologCtx = {
  XMLDeclOpen: IToken[];
  attribute: AttributeCstNode[];
  SPECIAL_CLOSE: IToken[];
};

export interface DocTypeDeclNode extends CstNode {
  name: "docTypeDecl";
  children: DocTypeDeclCtx;
}

export type DocTypeDeclCtx = {
  DocType: IToken[];
  Name: IToken[];
  externalID: ExternalIDNode[];
};

export interface ExternalIDNode extends CstNode {
  name: "ExternalIDNode";
  children: ExternalIDCtx;
}

export type ExternalIDCtx = {
  System: IToken[];
  Public: IToken[];
  PubIDLiteral: IToken[];
  SystemLiteral: IToken[];
};

export interface ContentCstNode extends CstNode {
  name: "content";
  children: ContentCtx;
}

export type ContentCtx = {
  chardata: ChardataCstNode[];
  element: ElementCstNode[];
  reference: ReferenceCstNode[];
  CData: IToken[];
  Comment: IToken[];
};

export interface ElementCstNode extends CstNode {
  name: "element";
  children: ElementCtx;
}

export type ElementCtx = {
  OPEN: IToken[];
  Name: IToken[];
  attribute: AttributeCstNode[];
  START_CLOSE: IToken[];
  content: ContentCstNode[];
  SLASH_OPEN: IToken[];
  END_NAME: IToken[];
  END: IToken[];
  SLASH_CLOSE: IToken[];
};

export interface ReferenceCstNode extends CstNode {
  name: "reference";
  children: ReferenceCtx;
}

export type ReferenceCtx = {
  EntityRef: IToken[];
  CharRef: IToken[];
};

export interface AttributeCstNode extends CstNode {
  name: "attribute";
  children: AttributeCtx;
}

export type AttributeCtx = {
  Name: IToken[];
  EQUALS: IToken[];
  STRING: IToken[];
};

export interface ChardataCstNode extends CstNode {
  name: "chardata";
  children: ChardataCtx;
}

export type ChardataCtx = {
  TEXT: IToken[];
  SEA_WS: IToken[];
};

export interface MiscCstNode extends CstNode {
  name: "misc";
  children: MiscCtx;
}

export type MiscCtx = {
  Comment: IToken[];
  PROCESSING_INSTRUCTION: IToken[];
  SEA_WS: IToken[];
};
