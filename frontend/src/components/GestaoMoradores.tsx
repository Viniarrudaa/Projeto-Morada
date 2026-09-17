import { FormEvent, useState } from 'react';
import { useMutation } from '@tanstack/react-query';
import { Plus, ShieldCheck, UserRound } from 'lucide-react';
import { createUser } from '../api/moradaApi';
import { getApiMessage } from '../api/client';
import { formatCpf, isValidCpf } from '../lib/documentNumbers';
import type { MoradaUser, UserRole } from '../types/morada';

type GestaoMoradoresProps = {
  users: MoradaUser[];
  isLoading: boolean;
  selectedUserId: number;
  onSelectUser(user: MoradaUser): void;
  onUserCreated(user: MoradaUser): void;
};

export function GestaoMoradores({ users, isLoading, selectedUserId, onSelectUser, onUserCreated }: GestaoMoradoresProps) {
  const [name, setName] = useState('');
  const [cpf, setCpf] = useState('');
  const [birthDate, setBirthDate] = useState('');
  const [password, setPassword] = useState('');
  const [role, setRole] = useState<UserRole>('USER');
  const [formError, setFormError] = useState('');

  const createUserMutation = useMutation({
    mutationFn: async () => {
      if (!isValidCpf(cpf)) {
        throw new Error('CPF inválido.');
      }
      if (!name.trim() || !birthDate || password.length < 6) {
        throw new Error('Preencha nome, nascimento e senha com pelo menos 6 caracteres.');
      }
      return createUser({ name, cpf, birthDate, password, role });
    },
    onSuccess: (user) => {
      setName('');
      setCpf('');
      setBirthDate('');
      setPassword('');
      setRole('USER');
      onUserCreated(user);
    },
    onError: (error) => setFormError(getApiMessage(error)),
  });

  function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setFormError('');
    createUserMutation.mutate();
  }

  return (
    <div className="morada-gestao-moradores">
      <div className="morada-lista-moradores" aria-busy={isLoading}>
        {users.map((user) => (
          <button
            className={user.id === selectedUserId ? 'morada-morador-linha morada-morador-linha--selecionado' : 'morada-morador-linha'}
            type="button"
            key={user.id}
            onClick={() => onSelectUser(user)}
          >
            <span>{user.name}</span>
            <small>{user.role === 'ADMIN' ? 'Admin' : 'Usuário'}</small>
          </button>
        ))}
        {!isLoading && users.length === 0 && <p className="morada-lista-vazia">Nenhum usuário cadastrado.</p>}
      </div>

      <form className="morada-formulario-acesso morada-formulario-morador" onSubmit={handleSubmit}>
        <h3>Novo usuário</h3>
        <label>
          Nome
          <input value={name} onChange={(event) => setName(event.target.value)} required />
        </label>
        <label>
          CPF
          <input value={cpf} inputMode="numeric" onChange={(event) => setCpf(formatCpf(event.target.value))} required />
        </label>
        <label>
          Data de nascimento
          <input type="date" value={birthDate} onChange={(event) => setBirthDate(event.target.value)} required />
        </label>
        <label>
          Senha
          <input type="password" value={password} onChange={(event) => setPassword(event.target.value)} required />
        </label>

        <div className="morada-seletor-perfil" aria-label="Perfil do usuário">
          <button
            className={role === 'USER' ? 'morada-opcao-perfil morada-opcao-perfil--ativa' : 'morada-opcao-perfil'}
            type="button"
            onClick={() => setRole('USER')}
          >
            <UserRound size={15} />
            Usuário
          </button>
          <button
            className={role === 'ADMIN' ? 'morada-opcao-perfil morada-opcao-perfil--ativa' : 'morada-opcao-perfil'}
            type="button"
            onClick={() => setRole('ADMIN')}
          >
            <ShieldCheck size={15} />
            Admin
          </button>
        </div>

        {formError && <p className="morada-aviso-formulario">{formError}</p>}

        <button className="morada-botao-criar-morador" type="submit" disabled={createUserMutation.isPending}>
          <Plus size={16} />
          {createUserMutation.isPending ? 'Criando...' : 'Criar usuário'}
        </button>
      </form>
    </div>
  );
}
