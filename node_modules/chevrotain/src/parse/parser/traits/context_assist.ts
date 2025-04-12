import {
  ISyntacticContentAssistPath,
  IToken,
  ITokenGrammarPath,
  TokenType
} from "../../../../api"
import {
  NextAfterTokenWalker,
  nextPossibleTokensAfter
} from "../../grammar/interpreter"
import { first, isUndefined } from "../../../utils/utils"
import { MixedInParser } from "./parser_traits"

export class ContentAssist {
  initContentAssist() {}

  public computeContentAssist(
    this: MixedInParser,
    startRuleName: string,
    precedingInput: IToken[]
  ): ISyntacticContentAssistPath[] {
    let startRuleGast = this.gastProductionsCache[startRuleName]

    if (isUndefined(startRuleGast)) {
      throw Error(`Rule ->${startRuleName}<- does not exist in this grammar.`)
    }

    return nextPossibleTokensAfter(
      [startRuleGast],
      precedingInput,
      this.tokenMatcher,
      this.maxLookahead
    )
  }

  // TODO: should this be a member method or a utility? it does not have any state or usage of 'this'...
  // TODO: should this be more explicitly part of the public API?
  public getNextPossibleTokenTypes(
    this: MixedInParser,
    grammarPath: ITokenGrammarPath
  ): TokenType[] {
    let topRuleName = first(grammarPath.ruleStack)
    let gastProductions = this.getGAstProductions()
    let topProduction = gastProductions[topRuleName]
    let nextPossibleTokenTypes = new NextAfterTokenWalker(
      topProduction,
      grammarPath
    ).startWalking()
    return nextPossibleTokenTypes
  }
}
