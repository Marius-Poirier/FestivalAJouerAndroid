export function sanitizeString(value: unknown) {
    return typeof value === 'string' ? value.trim() : ''
}

export function parseInteger(value: unknown, fieldName: string) {
    const numberValue = typeof value === 'string' ? Number.parseInt(value, 10) : value
    if (!Number.isInteger(numberValue)) {
        throw new Error(`Le champ ${fieldName} doit être un entier`)
    }
    return numberValue as number
}

export function parsePositiveInteger(value: unknown, fieldName: string) {
    const num = parseInteger(value, fieldName)
    if (num <= 0) {
        throw new Error(`Le champ ${fieldName} doit être un entier positif`)
    }
    return num
}

const DATE_REGEX = /^\d{4}-\d{2}-\d{2}$/

export function validateDateString(value: string, fieldName: string) {
    if (!value || !DATE_REGEX.test(value) || Number.isNaN(Date.parse(value))) {
        throw new Error(`Le champ ${fieldName} doit être une date valide au format AAAA-MM-JJ`)
    }
}

export function parseDecimal(value: unknown, fieldName: string) {
    const decimalValue = typeof value === 'string' ? Number.parseFloat(value) : value
    if (typeof decimalValue !== 'number' || Number.isNaN(decimalValue)) {
        throw new Error(`Le champ ${fieldName} doit être un nombre`)
    }
    return decimalValue
}
