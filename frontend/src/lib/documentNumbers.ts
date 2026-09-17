export function onlyDigits(value: string) {
  return value.replace(/\D/g, '');
}

export function formatCpf(value: string) {
  const digits = onlyDigits(value).slice(0, 11);
  return digits
    .replace(/^(\d{3})(\d)/, '$1.$2')
    .replace(/^(\d{3})\.(\d{3})(\d)/, '$1.$2.$3')
    .replace(/^(\d{3})\.(\d{3})\.(\d{3})(\d)/, '$1.$2.$3-$4');
}

export function isValidCpf(value: string) {
  const cpf = onlyDigits(value);
  if (cpf.length !== 11 || /^(\d)\1+$/.test(cpf)) {
    return false;
  }

  const calculateDigit = (base: string, weight: number) => {
    const sum = base.split('').reduce((total, digit) => {
      const next = total + Number(digit) * weight;
      weight -= 1;
      return next;
    }, 0);

    const result = 11 - (sum % 11);
    return result >= 10 ? 0 : result;
  };

  const firstDigit = calculateDigit(cpf.slice(0, 9), 10);
  const secondDigit = calculateDigit(`${cpf.slice(0, 9)}${firstDigit}`, 11);
  return cpf === `${cpf.slice(0, 9)}${firstDigit}${secondDigit}`;
}

export function formatCep(value: string) {
  const digits = onlyDigits(value).slice(0, 8);
  return digits.replace(/^(\d{5})(\d)/, '$1-$2');
}
