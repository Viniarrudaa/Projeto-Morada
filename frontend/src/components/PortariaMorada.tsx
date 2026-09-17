import { FormEvent, useState } from 'react';
import { useMutation } from '@tanstack/react-query';
import { KeyRound, LogIn, UserPlus } from 'lucide-react';
import { getApiMessage } from '../api/client';
import { login, register } from '../api/moradaApi';
import { formatCpf, isValidCpf } from '../lib/documentNumbers';
import type { MoradaSession } from '../types/morada';

type PortariaMoradaProps = {
  onAuthenticated(session: MoradaSession): void;
};

export function PortariaMorada({ onAuthenticated }: PortariaMoradaProps) {
  const [mode, setMode] = useState<'login' | 'register'>('login');
  const [name, setName] = useState('');
  const [cpf, setCpf] = useState('');
  const [birthDate, setBirthDate] = useState('');
  const [password, setPassword] = useState('');
  const [formError, setFormError] = useState('');

  const accessMutation = useMutation({
    mutationFn: async () => {
      if (!isValidCpf(cpf)) {
        throw new Error('CPF inválido.');
      }
      if (password.length < 6) {
        throw new Error('A senha precisa ter pelo menos 6 caracteres.');
      }
      if (mode === 'register') {
        if (!name.trim() || !birthDate) {
          throw new Error('Informe nome e data de nascimento.');
        }
        await register({ name, cpf, birthDate, password });
      }
      return login(cpf, password);
    },
    onSuccess: onAuthenticated,
    onError: (error) => setFormError(getApiMessage(error)),
  });

  function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setFormError('');
    accessMutation.mutate();
  }

  return (
    <main className="morada-portaria">
      <section className="morada-acesso">
        <div className="morada-acesso-cabecalho">
          <span className="morada-selo-app">
            <KeyRound size={16} />
            Morada
          </span>
          <h1>{mode === 'login' ? 'Entrar no painel' : 'Criar acesso'}</h1>
          <p>Use CPF e senha para gerenciar os endereços.</p>
        </div>

        <div className="morada-seletor-acesso" aria-label="Tipo de acesso">
          <button
            className={mode === 'login' ? 'morada-opcao-acesso morada-opcao-acesso--ativa' : 'morada-opcao-acesso'}
            type="button"
            onClick={() => setMode('login')}
          >
            Entrar
          </button>
          <button
            className={mode === 'register' ? 'morada-opcao-acesso morada-opcao-acesso--ativa' : 'morada-opcao-acesso'}
            type="button"
            onClick={() => setMode('register')}
          >
            Cadastro
          </button>
        </div>

        <form className="morada-formulario-acesso" onSubmit={handleSubmit}>
          {mode === 'register' && (
            <>
              <label>
                Nome
                <input value={name} onChange={(event) => setName(event.target.value)} required />
              </label>
              <label>
                Data de nascimento
                <input type="date" value={birthDate} onChange={(event) => setBirthDate(event.target.value)} required />
              </label>
            </>
          )}

          <label>
            CPF
            <input
              value={cpf}
              inputMode="numeric"
              placeholder="000.000.000-00"
              onChange={(event) => setCpf(formatCpf(event.target.value))}
              required
            />
          </label>

          <label>
            Senha
            <input type="password" value={password} onChange={(event) => setPassword(event.target.value)} required />
          </label>

          {formError && <p className="morada-aviso-formulario">{formError}</p>}

          <button className="morada-botao-acesso" type="submit" disabled={accessMutation.isPending}>
            {mode === 'login' ? <LogIn size={18} /> : <UserPlus size={18} />}
            {accessMutation.isPending ? 'Aguarde...' : mode === 'login' ? 'Entrar' : 'Cadastrar e entrar'}
          </button>
        </form>
      </section>
    </main>
  );
}
