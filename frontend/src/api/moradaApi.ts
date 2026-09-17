import { api } from './client';
import type { MoradaAddress, MoradaAddressDraft, MoradaCepDetails, MoradaSession, MoradaUser, UserRole } from '../types/morada';
import { onlyDigits } from '../lib/documentNumbers';

type MoradaUserDraft = {
  name: string;
  cpf: string;
  birthDate: string;
  password: string;
  role?: UserRole;
};

export async function login(cpf: string, password: string) {
  const authenticatedSession = await api.post<MoradaSession>('/auth/login', {
    cpf: onlyDigits(cpf),
    password,
  });
  return authenticatedSession.data;
}

export async function register(userDraft: MoradaUserDraft) {
  const registeredUser = await api.post<MoradaUser>('/auth/register', {
    ...userDraft,
    cpf: onlyDigits(userDraft.cpf),
  });
  return registeredUser.data;
}

export async function createUser(userDraft: MoradaUserDraft) {
  const createdUser = await api.post<MoradaUser>('/users', {
    ...userDraft,
    cpf: onlyDigits(userDraft.cpf),
  });
  return createdUser.data;
}

export async function getCurrentUser() {
  const signedUser = await api.get<MoradaUser>('/users/me');
  return signedUser.data;
}

export async function listUsers() {
  const registeredUsers = await api.get<MoradaUser[]>('/users');
  return registeredUsers.data;
}

export async function getUser(userId: number) {
  const selectedUser = await api.get<MoradaUser>(`/users/${userId}`);
  return selectedUser.data;
}

export async function listAddresses(userId: number) {
  const savedAddresses = await api.get<MoradaAddress[]>(`/users/${userId}/addresses`);
  return savedAddresses.data;
}

export async function lookupCep(cep: string) {
  const cepDetails = await api.get<MoradaCepDetails>(`/ceps/${onlyDigits(cep)}`);
  return cepDetails.data;
}

export async function createAddress(userId: number, addressDraft: MoradaAddressDraft) {
  const createdAddress = await api.post<MoradaAddress>(`/users/${userId}/addresses`, {
    ...addressDraft,
    cep: onlyDigits(addressDraft.cep),
  });
  return createdAddress.data;
}

export async function updateAddress(addressId: number, addressDraft: MoradaAddressDraft) {
  const updatedAddress = await api.put<MoradaAddress>(`/addresses/${addressId}`, {
    ...addressDraft,
    cep: onlyDigits(addressDraft.cep),
  });
  return updatedAddress.data;
}

export async function deleteAddress(addressId: number) {
  await api.delete(`/addresses/${addressId}`);
}

export async function setPrimaryAddress(addressId: number) {
  const promotedAddress = await api.patch<MoradaAddress>(`/addresses/${addressId}/primary`);
  return promotedAddress.data;
}
