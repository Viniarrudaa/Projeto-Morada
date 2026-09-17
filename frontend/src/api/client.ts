import axios from 'axios';
import type { MoradaApiProblem, MoradaSession } from '../types/morada';

const sessionKey = 'morada.session';

export const api = axios.create({
  baseURL: import.meta.env.VITE_API_URL ?? 'http://localhost:8080/api',
});

api.interceptors.request.use((config) => {
  const rawSession = localStorage.getItem(sessionKey);
  if (rawSession) {
    const session = JSON.parse(rawSession) as MoradaSession;
    config.headers.Authorization = `Bearer ${session.token}`;
  }
  return config;
});

export function getApiMessage(error: unknown) {
  if (axios.isAxiosError<MoradaApiProblem>(error) && error.response?.data?.message) {
    return error.response.data.message;
  }
  if (error instanceof Error) {
    return error.message;
  }
  return 'Não foi possível concluir a operação.';
}

export const moradaSessionStorage = {
  key: sessionKey,
  load() {
    const rawSession = localStorage.getItem(sessionKey);
    return rawSession ? (JSON.parse(rawSession) as MoradaSession) : null;
  },
  save(session: MoradaSession) {
    localStorage.setItem(sessionKey, JSON.stringify(session));
  },
  clear() {
    localStorage.removeItem(sessionKey);
  },
};
