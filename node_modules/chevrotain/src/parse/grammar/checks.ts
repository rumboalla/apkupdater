import * as utils from "../../utils/utils"
import {
  contains,
  every,
  findAll,
  flatten,
  forEach,
  isEmpty,
  map,
  reduce,
  reject
} from "../../utils/utils"
import {
  IParserAmbiguousAlternativesDefinitionError,
  IParserDuplicatesDefinitionError,
  IParserEmptyAlternativeDefinitionError,
  ParserDefinitionErrorType
} from "../parser/parser"
import { getProductionDslName, isOptionalProd } from "./gast/gast"
import {
  Alternative,
  containsPath,
  getLookaheadPathsForOptionalProd,
  getLookaheadPathsForOr,
  getProdType,
  isStrictPrefixOfPath
} from "./lookahead"
import { nextPossibleTokensAfter } from "./interpreter"
import {
  Alternation,
  Alternative as AlternativeGAST,
  NonTerminal,
  Option,
  Repetition,
  RepetitionMandatory,
  RepetitionMandatoryWithSeparator,
  RepetitionWithSeparator,
  Rule,
  Terminal
} from "./gast/gast_public"
import { GAstVisitor } from "./gast/gast_visitor_public"
import {
  IGrammarValidatorErrorMessageProvider,
  IParserDefinitionError,
  IProduction,
  IProductionWithOccurrence,
  TokenType
} from "../../../api"

export function validateGrammar(
  topLevels: Rule[],
  globalMaxLookahead: number,
  tokenTypes: TokenType[],
  errMsgProvider: IGrammarValidatorErrorMessageProvider,
  grammarName: string
): IParserDefinitionError[] {
  let duplicateErrors: any = utils.map(topLevels, (currTopLevel) =>
    validateDuplicateProductions(currTopLevel, errMsgProvider)
  )
  let leftRecursionErrors: any = utils.map(topLevels, (currTopRule) =>
    validateNoLeftRecursion(currTopRule, currTopRule, errMsgProvider)
  )

  let emptyAltErrors = []
  let ambiguousAltsErrors = []
  let emptyRepetitionErrors = []

  // left recursion could cause infinite loops in the following validations.
  // It is safest to first have the user fix the left recursion errors first and only then examine Further issues.
  if (every(leftRecursionErrors, isEmpty)) {
    emptyAltErrors = map(topLevels, (currTopRule) =>
      validateEmptyOrAlternative(currTopRule, errMsgProvider)
    )
    ambiguousAltsErrors = map(topLevels, (currTopRule) =>
      validateAmbiguousAlternationAlternatives(
        currTopRule,
        globalMaxLookahead,
        errMsgProvider
      )
    )

    emptyRepetitionErrors = validateSomeNonEmptyLookaheadPath(
      topLevels,
      globalMaxLookahead,
      errMsgProvider
    )
  }

  let termsNamespaceConflictErrors = checkTerminalAndNoneTerminalsNameSpace(
    topLevels,
    tokenTypes,
    errMsgProvider
  )

  const tooManyAltsErrors = map(topLevels, (curRule) =>
    validateTooManyAlts(curRule, errMsgProvider)
  )

  const duplicateRulesError = map(topLevels, (curRule) =>
    validateRuleDoesNotAlreadyExist(
      curRule,
      topLevels,
      grammarName,
      errMsgProvider
    )
  )

  return <any>(
    utils.flatten(
      duplicateErrors.concat(
        emptyRepetitionErrors,
        leftRecursionErrors,
        emptyAltErrors,
        ambiguousAltsErrors,
        termsNamespaceConflictErrors,
        tooManyAltsErrors,
        duplicateRulesError
      )
    )
  )
}

function validateDuplicateProductions(
  topLevelRule: Rule,
  errMsgProvider: IGrammarValidatorErrorMessageProvider
): IParserDuplicatesDefinitionError[] {
  let collectorVisitor = new OccurrenceValidationCollector()
  topLevelRule.accept(collectorVisitor)
  let allRuleProductions = collectorVisitor.allProductions

  let productionGroups = utils.groupBy(
    allRuleProductions,
    identifyProductionForDuplicates
  )

  let duplicates: any = utils.pick(productionGroups, (currGroup) => {
    return currGroup.length > 1
  })

  let errors = utils.map(utils.values(duplicates), (currDuplicates: any) => {
    let firstProd: any = utils.first(currDuplicates)
    let msg = errMsgProvider.buildDuplicateFoundError(
      topLevelRule,
      currDuplicates
    )
    let dslName = getProductionDslName(firstProd)
    let defError: IParserDuplicatesDefinitionError = {
      message: msg,
      type: ParserDefinitionErrorType.DUPLICATE_PRODUCTIONS,
      ruleName: topLevelRule.name,
      dslName: dslName,
      occurrence: firstProd.idx
    }

    let param = getExtraProductionArgument(firstProd)
    if (param) {
      defError.parameter = param
    }

    return defError
  })
  return errors
}

export function identifyProductionForDuplicates(
  prod: IProductionWithOccurrence
): string {
  return `${getProductionDslName(prod)}_#_${
    prod.idx
  }_#_${getExtraProductionArgument(prod)}`
}

function getExtraProductionArgument(prod: IProductionWithOccurrence): string {
  if (prod instanceof Terminal) {
    return prod.terminalType.name
  } else if (prod instanceof NonTerminal) {
    return prod.nonTerminalName
  } else {
    return ""
  }
}

export class OccurrenceValidationCollector extends GAstVisitor {
  public allProductions: IProduction[] = []

  public visitNonTerminal(subrule: NonTerminal): void {
    this.allProductions.push(subrule)
  }

  public visitOption(option: Option): void {
    this.allProductions.push(option)
  }

  public visitRepetitionWithSeparator(manySep: RepetitionWithSeparator): void {
    this.allProductions.push(manySep)
  }

  public visitRepetitionMandatory(atLeastOne: RepetitionMandatory): void {
    this.allProductions.push(atLeastOne)
  }

  public visitRepetitionMandatoryWithSeparator(
    atLeastOneSep: RepetitionMandatoryWithSeparator
  ): void {
    this.allProductions.push(atLeastOneSep)
  }

  public visitRepetition(many: Repetition): void {
    this.allProductions.push(many)
  }

  public visitAlternation(or: Alternation): void {
    this.allProductions.push(or)
  }

  public visitTerminal(terminal: Terminal): void {
    this.allProductions.push(terminal)
  }
}

export function validateRuleDoesNotAlreadyExist(
  rule: Rule,
  allRules: Rule[],
  className,
  errMsgProvider: IGrammarValidatorErrorMessageProvider
): IParserDefinitionError[] {
  let errors = []
  const occurrences = reduce(
    allRules,
    (result, curRule) => {
      if (curRule.name === rule.name) {
        return result + 1
      }
      return result
    },
    0
  )
  if (occurrences > 1) {
    const errMsg = errMsgProvider.buildDuplicateRuleNameError({
      topLevelRule: rule,
      grammarName: className
    })
    errors.push({
      message: errMsg,
      type: ParserDefinitionErrorType.DUPLICATE_RULE_NAME,
      ruleName: rule.name
    })
  }

  return errors
}

// TODO: is there anyway to get only the rule names of rules inherited from the super grammars?
// This is not part of the IGrammarErrorProvider because the validation cannot be performed on
// The grammar structure, only at runtime.
export function validateRuleIsOverridden(
  ruleName: string,
  definedRulesNames: string[],
  className
): IParserDefinitionError[] {
  let errors = []
  let errMsg

  if (!utils.contains(definedRulesNames, ruleName)) {
    errMsg =
      `Invalid rule override, rule: ->${ruleName}<- cannot be overridden in the grammar: ->${className}<-` +
      `as it is not defined in any of the super grammars `
    errors.push({
      message: errMsg,
      type: ParserDefinitionErrorType.INVALID_RULE_OVERRIDE,
      ruleName: ruleName
    })
  }

  return errors
}

export function validateNoLeftRecursion(
  topRule: Rule,
  currRule: Rule,
  errMsgProvider: IGrammarValidatorErrorMessageProvider,
  path: Rule[] = []
): IParserDefinitionError[] {
  let errors = []
  let nextNonTerminals = getFirstNoneTerminal(currRule.definition)
  if (utils.isEmpty(nextNonTerminals)) {
    return []
  } else {
    let ruleName = topRule.name
    let foundLeftRecursion = utils.contains(<any>nextNonTerminals, topRule)
    if (foundLeftRecursion) {
      errors.push({
        message: errMsgProvider.buildLeftRecursionError({
          topLevelRule: topRule,
          leftRecursionPath: path
        }),
        type: ParserDefinitionErrorType.LEFT_RECURSION,
        ruleName: ruleName
      })
    }

    // we are only looking for cyclic paths leading back to the specific topRule
    // other cyclic paths are ignored, we still need this difference to avoid infinite loops...
    let validNextSteps = utils.difference(
      nextNonTerminals,
      path.concat([topRule])
    )
    let errorsFromNextSteps = utils.map(validNextSteps, (currRefRule) => {
      let newPath = utils.cloneArr(path)
      newPath.push(currRefRule)
      return validateNoLeftRecursion(
        topRule,
        currRefRule,
        errMsgProvider,
        newPath
      )
    })

    return errors.concat(utils.flatten(errorsFromNextSteps))
  }
}

export function getFirstNoneTerminal(definition: IProduction[]): Rule[] {
  let result = []
  if (utils.isEmpty(definition)) {
    return result
  }
  let firstProd = utils.first(definition)

  /* istanbul ignore else */
  if (firstProd instanceof NonTerminal) {
    result.push(firstProd.referencedRule)
  } else if (
    firstProd instanceof AlternativeGAST ||
    firstProd instanceof Option ||
    firstProd instanceof RepetitionMandatory ||
    firstProd instanceof RepetitionMandatoryWithSeparator ||
    firstProd instanceof RepetitionWithSeparator ||
    firstProd instanceof Repetition
  ) {
    result = result.concat(
      getFirstNoneTerminal(<IProduction[]>firstProd.definition)
    )
  } else if (firstProd instanceof Alternation) {
    // each sub definition in alternation is a FLAT
    result = utils.flatten(
      utils.map(firstProd.definition, (currSubDef) =>
        getFirstNoneTerminal((<AlternativeGAST>currSubDef).definition)
      )
    )
  } else if (firstProd instanceof Terminal) {
    // nothing to see, move along
  } else {
    throw Error("non exhaustive match")
  }

  let isFirstOptional = isOptionalProd(firstProd)
  let hasMore = definition.length > 1
  if (isFirstOptional && hasMore) {
    let rest = utils.drop(definition)
    return result.concat(getFirstNoneTerminal(rest))
  } else {
    return result
  }
}

class OrCollector extends GAstVisitor {
  public alternations = []

  public visitAlternation(node: Alternation): void {
    this.alternations.push(node)
  }
}

export function validateEmptyOrAlternative(
  topLevelRule: Rule,
  errMsgProvider: IGrammarValidatorErrorMessageProvider
): IParserEmptyAlternativeDefinitionError[] {
  let orCollector = new OrCollector()
  topLevelRule.accept(orCollector)
  let ors = orCollector.alternations

  let errors = utils.reduce(
    ors,
    (errors, currOr) => {
      let exceptLast = utils.dropRight(currOr.definition)
      let currErrors = utils.map(
        exceptLast,
        (currAlternative: IProduction, currAltIdx) => {
          const possibleFirstInAlt = nextPossibleTokensAfter(
            [currAlternative],
            [],
            null,
            1
          )
          if (utils.isEmpty(possibleFirstInAlt)) {
            return {
              message: errMsgProvider.buildEmptyAlternationError({
                topLevelRule: topLevelRule,
                alternation: currOr,
                emptyChoiceIdx: currAltIdx
              }),
              type: ParserDefinitionErrorType.NONE_LAST_EMPTY_ALT,
              ruleName: topLevelRule.name,
              occurrence: currOr.idx,
              alternative: currAltIdx + 1
            }
          } else {
            return null
          }
        }
      )
      return errors.concat(utils.compact(currErrors))
    },
    []
  )

  return errors
}

export function validateAmbiguousAlternationAlternatives(
  topLevelRule: Rule,
  globalMaxLookahead: number,
  errMsgProvider: IGrammarValidatorErrorMessageProvider
): IParserAmbiguousAlternativesDefinitionError[] {
  let orCollector = new OrCollector()
  topLevelRule.accept(orCollector)
  let ors = orCollector.alternations

  // New Handling of ignoring ambiguities
  // - https://github.com/SAP/chevrotain/issues/869
  ors = reject(ors, (currOr) => currOr.ignoreAmbiguities === true)

  let errors = utils.reduce(
    ors,
    (result, currOr: Alternation) => {
      let currOccurrence = currOr.idx
      const actualMaxLookahead = currOr.maxLookahead || globalMaxLookahead
      let alternatives = getLookaheadPathsForOr(
        currOccurrence,
        topLevelRule,
        actualMaxLookahead,
        currOr
      )
      let altsAmbiguityErrors = checkAlternativesAmbiguities(
        alternatives,
        currOr,
        topLevelRule,
        errMsgProvider
      )
      let altsPrefixAmbiguityErrors = checkPrefixAlternativesAmbiguities(
        alternatives,
        currOr,
        topLevelRule,
        errMsgProvider
      )

      return result.concat(altsAmbiguityErrors, altsPrefixAmbiguityErrors)
    },
    []
  )

  return errors
}

export class RepetionCollector extends GAstVisitor {
  public allProductions: IProduction[] = []

  public visitRepetitionWithSeparator(manySep: RepetitionWithSeparator): void {
    this.allProductions.push(manySep)
  }

  public visitRepetitionMandatory(atLeastOne: RepetitionMandatory): void {
    this.allProductions.push(atLeastOne)
  }

  public visitRepetitionMandatoryWithSeparator(
    atLeastOneSep: RepetitionMandatoryWithSeparator
  ): void {
    this.allProductions.push(atLeastOneSep)
  }

  public visitRepetition(many: Repetition): void {
    this.allProductions.push(many)
  }
}

export function validateTooManyAlts(
  topLevelRule: Rule,
  errMsgProvider: IGrammarValidatorErrorMessageProvider
): IParserDefinitionError[] {
  let orCollector = new OrCollector()
  topLevelRule.accept(orCollector)
  let ors = orCollector.alternations

  let errors = utils.reduce(
    ors,
    (errors, currOr) => {
      if (currOr.definition.length > 255) {
        errors.push({
          message: errMsgProvider.buildTooManyAlternativesError({
            topLevelRule: topLevelRule,
            alternation: currOr
          }),
          type: ParserDefinitionErrorType.TOO_MANY_ALTS,
          ruleName: topLevelRule.name,
          occurrence: currOr.idx
        })
      }
      return errors
    },
    []
  )

  return errors
}

export function validateSomeNonEmptyLookaheadPath(
  topLevelRules: Rule[],
  maxLookahead: number,
  errMsgProvider: IGrammarValidatorErrorMessageProvider
): IParserDefinitionError[] {
  let errors = []
  forEach(topLevelRules, (currTopRule) => {
    let collectorVisitor = new RepetionCollector()
    currTopRule.accept(collectorVisitor)
    let allRuleProductions = collectorVisitor.allProductions
    forEach(allRuleProductions, (currProd) => {
      let prodType = getProdType(currProd)
      const actualMaxLookahead = currProd.maxLookahead || maxLookahead
      let currOccurrence = currProd.idx
      let paths = getLookaheadPathsForOptionalProd(
        currOccurrence,
        currTopRule,
        prodType,
        actualMaxLookahead
      )
      let pathsInsideProduction = paths[0]
      if (isEmpty(flatten(pathsInsideProduction))) {
        const errMsg = errMsgProvider.buildEmptyRepetitionError({
          topLevelRule: currTopRule,
          repetition: currProd
        })
        errors.push({
          message: errMsg,
          type: ParserDefinitionErrorType.NO_NON_EMPTY_LOOKAHEAD,
          ruleName: currTopRule.name
        })
      }
    })
  })

  return errors
}

export interface IAmbiguityDescriptor {
  alts: number[]
  path: TokenType[]
}

function checkAlternativesAmbiguities(
  alternatives: Alternative[],
  alternation: Alternation,
  rule: Rule,
  errMsgProvider: IGrammarValidatorErrorMessageProvider
): IParserAmbiguousAlternativesDefinitionError[] {
  let foundAmbiguousPaths = []
  let identicalAmbiguities = reduce(
    alternatives,
    (result, currAlt, currAltIdx) => {
      // ignore (skip) ambiguities with this alternative
      if (alternation.definition[currAltIdx].ignoreAmbiguities === true) {
        return result
      }

      forEach(currAlt, (currPath) => {
        let altsCurrPathAppearsIn = [currAltIdx]
        forEach(alternatives, (currOtherAlt, currOtherAltIdx) => {
          if (
            currAltIdx !== currOtherAltIdx &&
            containsPath(currOtherAlt, currPath) &&
            // ignore (skip) ambiguities with this "other" alternative
            alternation.definition[currOtherAltIdx].ignoreAmbiguities !== true
          ) {
            altsCurrPathAppearsIn.push(currOtherAltIdx)
          }
        })

        if (
          altsCurrPathAppearsIn.length > 1 &&
          !containsPath(foundAmbiguousPaths, currPath)
        ) {
          foundAmbiguousPaths.push(currPath)
          result.push({
            alts: altsCurrPathAppearsIn,
            path: currPath
          })
        }
      })
      return result
    },
    []
  )

  let currErrors = utils.map(identicalAmbiguities, (currAmbDescriptor) => {
    let ambgIndices = map(
      currAmbDescriptor.alts,
      (currAltIdx) => currAltIdx + 1
    )

    const currMessage = errMsgProvider.buildAlternationAmbiguityError({
      topLevelRule: rule,
      alternation: alternation,
      ambiguityIndices: ambgIndices,
      prefixPath: currAmbDescriptor.path
    })

    return {
      message: currMessage,
      type: ParserDefinitionErrorType.AMBIGUOUS_ALTS,
      ruleName: rule.name,
      occurrence: alternation.idx,
      alternatives: [currAmbDescriptor.alts]
    }
  })

  return currErrors
}

export function checkPrefixAlternativesAmbiguities(
  alternatives: Alternative[],
  alternation: Alternation,
  rule: Rule,
  errMsgProvider: IGrammarValidatorErrorMessageProvider
): IAmbiguityDescriptor[] {
  let errors = []

  // flatten
  let pathsAndIndices = reduce(
    alternatives,
    (result, currAlt, idx) => {
      let currPathsAndIdx = map(currAlt, (currPath) => {
        return { idx: idx, path: currPath }
      })
      return result.concat(currPathsAndIdx)
    },
    []
  )

  forEach(pathsAndIndices, (currPathAndIdx) => {
    const alternativeGast = alternation.definition[currPathAndIdx.idx]
    // ignore (skip) ambiguities with this alternative
    if (alternativeGast.ignoreAmbiguities === true) {
      return
    }
    let targetIdx = currPathAndIdx.idx
    let targetPath = currPathAndIdx.path

    let prefixAmbiguitiesPathsAndIndices = findAll(
      pathsAndIndices,
      (searchPathAndIdx) => {
        // prefix ambiguity can only be created from lower idx (higher priority) path
        return (
          // ignore (skip) ambiguities with this "other" alternative
          alternation.definition[searchPathAndIdx.idx].ignoreAmbiguities !==
            true &&
          searchPathAndIdx.idx < targetIdx &&
          // checking for strict prefix because identical lookaheads
          // will be be detected using a different validation.
          isStrictPrefixOfPath(searchPathAndIdx.path, targetPath)
        )
      }
    )

    let currPathPrefixErrors = map(
      prefixAmbiguitiesPathsAndIndices,
      (currAmbPathAndIdx) => {
        let ambgIndices = [currAmbPathAndIdx.idx + 1, targetIdx + 1]
        const occurrence = alternation.idx === 0 ? "" : alternation.idx

        const message = errMsgProvider.buildAlternationPrefixAmbiguityError({
          topLevelRule: rule,
          alternation: alternation,
          ambiguityIndices: ambgIndices,
          prefixPath: currAmbPathAndIdx.path
        })
        return {
          message: message,
          type: ParserDefinitionErrorType.AMBIGUOUS_PREFIX_ALTS,
          ruleName: rule.name,
          occurrence: occurrence,
          alternatives: ambgIndices
        }
      }
    )
    errors = errors.concat(currPathPrefixErrors)
  })

  return errors
}

function checkTerminalAndNoneTerminalsNameSpace(
  topLevels: Rule[],
  tokenTypes: TokenType[],
  errMsgProvider: IGrammarValidatorErrorMessageProvider
): IParserDefinitionError[] {
  let errors = []

  let tokenNames = map(tokenTypes, (currToken) => currToken.name)

  forEach(topLevels, (currRule) => {
    const currRuleName = currRule.name
    if (contains(tokenNames, currRuleName)) {
      let errMsg = errMsgProvider.buildNamespaceConflictError(currRule)

      errors.push({
        message: errMsg,
        type: ParserDefinitionErrorType.CONFLICT_TOKENS_RULES_NAMESPACE,
        ruleName: currRuleName
      })
    }
  })

  return errors
}
