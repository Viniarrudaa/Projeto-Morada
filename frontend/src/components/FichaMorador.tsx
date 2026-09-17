import { ShieldCheck, UserRound } from 'lucide-react';
import type { MoradaUser } from '../types/morada';
import { formatCpf } from '../lib/documentNumbers';

type FichaMoradorProps = {
  user: MoradaUser;
  isOwnProfile: boolean;
};

export function FichaMorador({ user, isOwnProfile }: FichaMoradorProps) {
  return (
    <section className="morada-ficha-morador">
      <div>
        <span className="morada-rotulo-ficha">{isOwnProfile ? 'Seu cadastro' : 'Cadastro selecionado'}</span>
        <h1>{user.name}</h1>
      </div>
      <dl className="morada-dados-morador">
        <div>
          <dt>CPF</dt>
          <dd>{formatCpf(user.cpf)}</dd>
        </div>
        <div>
          <dt>Nascimento</dt>
          <dd>{new Date(`${user.birthDate}T00:00:00`).toLocaleDateString('pt-BR')}</dd>
        </div>
        <div>
          <dt>Perfil</dt>
          <dd className="morada-selo-perfil">
            {user.role === 'ADMIN' ? <ShieldCheck size={15} /> : <UserRound size={15} />}
            {user.role === 'ADMIN' ? 'Administrador' : 'Usuário'}
          </dd>
        </div>
      </dl>
    </section>
  );
}
