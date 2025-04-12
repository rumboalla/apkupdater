import { Rule } from "./gast_public"

import { defaults, forEach } from "../../../utils/utils"
import { resolveGrammar as orgResolveGrammar } from "../resolver"
import { validateGrammar as orgValidateGrammar } from "../checks"
import {
  defaultGrammarResolverErrorProvider,
  defaultGrammarValidatorErrorProvider
} from "../../errors_public"
import { DslMethodsCollectorVisitor } from "./gast"
import {
  IGrammarResolverErrorMessageProvider,
  IGrammarValidatorErrorMessageProvider,
  IParserDefinitionError,
  IProductionWithOccurrence,
  TokenType
} from "../../../../api"

export function resolveGrammar(options: {
  rules: Rule[]
  errMsgProvider?: IGrammarResolverErrorMessageProvider
}): IParserDefinitionError[] {
  options = defaults(options, {
    errMsgProvider: defaultGrammarResolverErrorProvider
  })

  const topRulesTable = {}
  forEach(options.rules, (rule) => {
    topRulesTable[rule.name] = rule
  })
  return orgResolveGrammar(topRulesTable, options.errMsgProvider)
}

export function validateGrammar(options: {
  rules: Rule[]
  maxLookahead: number
  tokenTypes: TokenType[]
  grammarName: string
  errMsgProvider: IGrammarValidatorErrorMessageProvider
}): IParserDefinitionError[] {
  options = defaults(options, {
    errMsgProvider: defaultGrammarValidatorErrorProvider
  })

  return orgValidateGrammar(
    options.rules,
    options.maxLookahead,
    options.tokenTypes,
    options.errMsgProvider,
    options.grammarName
  )
}

export function assignOccurrenceIndices(options: { rules: Rule[] }): void {
  forEach(options.rules, (currRule) => {
    const methodsCollector = new DslMethodsCollectorVisitor()
    currRule.accept(methodsCollector)
    forEach(methodsCollector.dslMethods, (methods) => {
      forEach(methods, (currMethod: IProductionWithOccurrence, arrIdx) => {
        currMethod.idx = arrIdx + 1
      })
    })
  })
}
