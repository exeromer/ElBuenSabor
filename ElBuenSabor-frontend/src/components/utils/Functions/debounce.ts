/**
 * @function debounce
 * @description Crea una versión "debounced" de una función que retrasa su ejecución
 * hasta que haya pasado un tiempo `waitFor` sin que se llame de nuevo.
 * @param {F} func - La función a "debouncear".
 * @param {number} waitFor - El tiempo en milisegundos a esperar.
 * @returns La función "debounced".
 */
export function debounce<F extends (...args: any[]) => any>(func: F, waitFor: number) {
    let timeout: ReturnType<typeof setTimeout> | null = null;

    const debounced = (...args: Parameters<F>) => {
        if (timeout !== null) {
            clearTimeout(timeout);
            timeout = null;
        }
        timeout = setTimeout(() => func(...args), waitFor);
    };
    return debounced as (...args: Parameters<F>) => ReturnType<F>;
}