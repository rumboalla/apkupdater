/*
 Utils using lodash style API. (not necessarily 100% compliant) for functional and other utils.
 These utils should replace usage of lodash in the production code base. not because they are any better...
 but for the purpose of being a dependency free library.

 The hotspots in the code are already written in imperative style for performance reasons.
 so writing several dozen utils which may be slower than the original lodash, does not matter as much
 considering they will not be invoked in hotspots...
 */

export function isEmpty(arr: any[]): boolean {
  return arr && arr.length === 0
}

export function keys(obj: any): string[] {
  if (obj === undefined || obj === null) {
    return []
  }
  return Object.keys(obj)
}

export function values(obj: any): any[] {
  let vals = []
  let keys = Object.keys(obj)
  for (let i = 0; i < keys.length; i++) {
    vals.push(obj[keys[i]])
  }
  return vals
}

export function mapValues<I, O>(
  obj: Object,
  callback: (value: I, key?: string) => O
): O[] {
  let result: O[] = []
  let objKeys = keys(obj)
  for (let idx = 0; idx < objKeys.length; idx++) {
    let currKey = objKeys[idx]
    result.push(callback.call(null, obj[currKey], currKey))
  }
  return result
}

export function map<I, O>(arr: I[], callback: (I, idx?: number) => O): O[] {
  let result: O[] = []
  for (let idx = 0; idx < arr.length; idx++) {
    result.push(callback.call(null, arr[idx], idx))
  }
  return result
}

export function flatten<T>(arr: any[]): T[] {
  let result = []

  for (let idx = 0; idx < arr.length; idx++) {
    let currItem = arr[idx]
    if (Array.isArray(currItem)) {
      result = result.concat(flatten(currItem))
    } else {
      result.push(currItem)
    }
  }
  return result
}

export function first<T>(arr: T[]): T {
  return isEmpty(arr) ? undefined : arr[0]
}

export function last<T>(arr: T[]): T {
  let len = arr && arr.length
  return len ? arr[len - 1] : undefined
}

export function forEach(collection: any, iteratorCallback: Function): void {
  /* istanbul ignore else */
  if (Array.isArray(collection)) {
    for (let i = 0; i < collection.length; i++) {
      iteratorCallback.call(null, collection[i], i)
    }
  } else if (isObject(collection)) {
    let colKeys = keys(collection)
    for (let i = 0; i < colKeys.length; i++) {
      let key = colKeys[i]
      let value = collection[key]
      iteratorCallback.call(null, value, key)
    }
  } else {
    throw Error("non exhaustive match")
  }
}

export function isString(item: any): boolean {
  return typeof item === "string"
}

export function isUndefined(item: any): boolean {
  return item === undefined
}

export function isFunction(item: any): boolean {
  return item instanceof Function
}

export function drop<T>(arr: T[], howMuch: number = 1): T[] {
  return arr.slice(howMuch, arr.length)
}

export function dropRight<T>(arr: T[], howMuch: number = 1): T[] {
  return arr.slice(0, arr.length - howMuch)
}

export function filter<T>(arr: T[], predicate: (T) => boolean): T[] {
  let result = []
  if (Array.isArray(arr)) {
    for (let i = 0; i < arr.length; i++) {
      let item = arr[i]
      if (predicate.call(null, item)) {
        result.push(item)
      }
    }
  }
  return result
}

export function reject<T>(arr: T[], predicate: (T) => boolean): T[] {
  return filter(arr, (item) => !predicate(item))
}

export function pick(obj: Object, predicate: (item) => boolean) {
  let keys = Object.keys(obj)
  let result = {}

  for (let i = 0; i < keys.length; i++) {
    let currKey = keys[i]
    let currItem = obj[currKey]
    if (predicate(currItem)) {
      result[currKey] = currItem
    }
  }

  return result
}

export function has(obj: any, prop: string): boolean {
  if (isObject(obj)) {
    return obj.hasOwnProperty(prop)
  }
  return false
}

export function contains<T>(arr: T[], item): boolean {
  return find(arr, (currItem) => currItem === item) !== undefined ? true : false
}

/**
 * shallow clone
 */
export function cloneArr<T>(arr: T[]): T[] {
  let newArr = []
  for (let i = 0; i < arr.length; i++) {
    newArr.push(arr[i])
  }
  return newArr
}

/**
 * shallow clone
 */
export function cloneObj(obj: Object): any {
  let clonedObj = {}
  for (let key in obj) {
    /* istanbul ignore else */
    if (Object.prototype.hasOwnProperty.call(obj, key)) {
      clonedObj[key] = obj[key]
    }
  }
  return clonedObj
}

export function find<T>(arr: T[], predicate: (item: T) => boolean): T {
  for (let i = 0; i < arr.length; i++) {
    let item = arr[i]
    if (predicate.call(null, item)) {
      return item
    }
  }
  return undefined
}

export function findAll<T>(arr: T[], predicate: (item: T) => boolean): T[] {
  let found = []
  for (let i = 0; i < arr.length; i++) {
    let item = arr[i]
    if (predicate.call(null, item)) {
      found.push(item)
    }
  }
  return found
}

export function reduce<T, A>(
  arrOrObj: Array<T> | Object,
  iterator: (result: A, item, idx?) => A,
  initial: A
): A {
  const isArr = Array.isArray(arrOrObj)

  let vals: T[] = isArr ? <Array<T>>arrOrObj : values(arrOrObj)
  let objKeys = isArr ? [] : keys(arrOrObj)

  let accumulator = initial
  for (let i = 0; i < vals.length; i++) {
    accumulator = iterator.call(
      null,
      accumulator,
      vals[i],
      isArr ? i : objKeys[i]
    )
  }
  return accumulator
}

export function compact<T>(arr: T[]): T[] {
  return reject(arr, (item) => item === null || item === undefined)
}

export function uniq<T>(
  arr: T[],
  identity: (item: T) => any = (item) => item
): T[] {
  let identities = []
  return reduce(
    arr,
    (result, currItem) => {
      let currIdentity = identity(currItem)
      if (contains(identities, currIdentity)) {
        return result
      } else {
        identities.push(currIdentity)
        return result.concat(currItem)
      }
    },
    []
  )
}

export function partial(func: Function, ...restArgs: any[]): Function {
  let firstArg = [null]
  let allArgs = firstArg.concat(restArgs)
  return Function.bind.apply(func, allArgs)
}

export function isArray(obj: any): obj is any[] {
  return Array.isArray(obj)
}

export function isRegExp(obj: any): obj is RegExp {
  return obj instanceof RegExp
}

export function isObject(obj: any): obj is Object {
  return obj instanceof Object
}

export function every<T>(
  arr: T[],
  predicate: (item: T, idx?) => boolean
): boolean {
  for (let i = 0; i < arr.length; i++) {
    if (!predicate(arr[i], i)) {
      return false
    }
  }
  return true
}

export function difference<T>(arr: T[], values: T[]): T[] {
  return reject(arr, (item) => contains(values, item))
}

export function some<T>(arr: T[], predicate: (item: T) => boolean): boolean {
  for (let i = 0; i < arr.length; i++) {
    if (predicate(arr[i])) {
      return true
    }
  }
  return false
}

export function indexOf<T>(arr: T[], value: T): number {
  for (let i = 0; i < arr.length; i++) {
    if (arr[i] === value) {
      return i
    }
  }
  return -1
}

export function sortBy<T>(arr: T[], orderFunc: (item: T) => number): T[] {
  let result = cloneArr(arr)
  result.sort((a, b) => orderFunc(a) - orderFunc(b))
  return result
}

export function zipObject(keys: any[], values: any[]): Object {
  if (keys.length !== values.length) {
    throw Error("can't zipObject with different number of keys and values!")
  }

  let result = {}
  for (let i = 0; i < keys.length; i++) {
    result[keys[i]] = values[i]
  }
  return result
}

/**
 * mutates! (and returns) target
 */
export function assign(target: Object, ...sources: Object[]): Object {
  for (let i = 0; i < sources.length; i++) {
    let curSource = sources[i]
    let currSourceKeys = keys(curSource)
    for (let j = 0; j < currSourceKeys.length; j++) {
      let currKey = currSourceKeys[j]
      target[currKey] = curSource[currKey]
    }
  }
  return target
}

/**
 * mutates! (and returns) target
 */
export function assignNoOverwrite(
  target: Object,
  ...sources: Object[]
): Object {
  for (let i = 0; i < sources.length; i++) {
    let curSource = sources[i]
    let currSourceKeys = keys(curSource)
    for (let j = 0; j < currSourceKeys.length; j++) {
      let currKey = currSourceKeys[j]
      if (!has(target, currKey)) {
        target[currKey] = curSource[currKey]
      }
    }
  }
  return target
}

export function defaults(...sources: any[]): any {
  return assignNoOverwrite.apply(null, [{}].concat(sources))
}

export function groupBy<T>(
  arr: T[],
  groupKeyFunc: (item: T) => string
): { [groupKey: string]: T[] } {
  let result: { [groupKey: string]: T[] } = {}

  forEach(arr, (item) => {
    let currGroupKey = groupKeyFunc(item)
    let currGroupArr = result[currGroupKey]

    if (currGroupArr) {
      currGroupArr.push(item)
    } else {
      result[currGroupKey] = [item]
    }
  })

  return result
}

/**
 * Merge obj2 into obj1.
 * Will overwrite existing properties with the same name
 */
export function merge(obj1: Object, obj2: Object): any {
  let result = cloneObj(obj1)
  let keys2 = keys(obj2)
  for (let i = 0; i < keys2.length; i++) {
    let key = keys2[i]
    let value = obj2[key]
    result[key] = value
  }

  return result
}

export function NOOP() {}

export function IDENTITY(item) {
  return item
}

/**
 * Will return a new packed array with same values.
 */
export function packArray<T>(holeyArr: T[]): T[] {
  const result = []
  for (let i = 0; i < holeyArr.length; i++) {
    const orgValue = holeyArr[i]
    result.push(orgValue !== undefined ? orgValue : undefined)
  }
  return result
}

export function PRINT_ERROR(msg) {
  /* istanbul ignore else - can't override global.console in node.js */
  if (console && console.error) {
    console.error(`Error: ${msg}`)
  }
}

export function PRINT_WARNING(msg) {
  /* istanbul ignore else - can't override global.console in node.js*/
  if (console && console.warn) {
    // TODO: modify docs accordingly
    console.warn(`Warning: ${msg}`)
  }
}

export function isES2015MapSupported(): boolean {
  return typeof Map === "function"
}

export function applyMixins(derivedCtor: any, baseCtors: any[]) {
  baseCtors.forEach((baseCtor) => {
    const baseProto = baseCtor.prototype
    Object.getOwnPropertyNames(baseProto).forEach((propName) => {
      if (propName === "constructor") {
        return
      }

      const basePropDescriptor = Object.getOwnPropertyDescriptor(
        baseProto,
        propName
      )
      // Handle Accessors
      if (
        basePropDescriptor &&
        (basePropDescriptor.get || basePropDescriptor.set)
      ) {
        Object.defineProperty(
          derivedCtor.prototype,
          propName,
          basePropDescriptor
        )
      } else {
        derivedCtor.prototype[propName] = baseCtor.prototype[propName]
      }
    })
  })
}

// base on: https://github.com/petkaantonov/bluebird/blob/b97c0d2d487e8c5076e8bd897e0dcd4622d31846/src/util.js#L201-L216
export function toFastProperties(toBecomeFast) {
  function FakeConstructor() {}
  // If our object is used as a constructor it would receive
  FakeConstructor.prototype = toBecomeFast
  const fakeInstance = new FakeConstructor()
  function fakeAccess() {
    return typeof fakeInstance.bar
  }
  // help V8 understand this is a "real" prototype by actually using
  // the fake instance.
  fakeAccess()
  fakeAccess()

  return toBecomeFast
  // Eval prevents optimization of this method (even though this is dead code)
  /* istanbul ignore next */
  // tslint:disable-next-line
  eval(toBecomeFast)
}

export function peek<T>(arr: T[]): T {
  return arr[arr.length - 1]
}

/* istanbul ignore next - for performance tracing*/
export function timer<T>(func: () => T): { time: number; value: T } {
  const start = new Date().getTime()
  const val = func()
  const end = new Date().getTime()
  const total = end - start
  return { time: total, value: val }
}
