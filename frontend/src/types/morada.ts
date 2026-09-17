export type UserRole = 'ADMIN' | 'USER';

export type MoradaUser = {
  id: number;
  name: string;
  cpf: string;
  birthDate: string;
  role: UserRole;
};

export type MoradaAddress = {
  id: number;
  userId: number;
  cep: string;
  number: string;
  complement?: string | null;
  street: string;
  district: string;
  city: string;
  state: string;
  primaryAddress: boolean;
};

export type MoradaCepDetails = {
  cep: string;
  street: string;
  district: string;
  city: string;
  state: string;
};

export type MoradaAddressDraft = {
  cep: string;
  number: string;
  complement?: string;
  primaryAddress: boolean;
};

export type MoradaSession = {
  token: string;
  user: MoradaUser;
};

export type MoradaApiProblem = {
  status: number;
  code: string;
  message: string;
  details?: string[];
};
