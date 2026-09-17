import { LogOut, MapPin, Users } from 'lucide-react';
import { useQuery, useQueryClient } from '@tanstack/react-query';
import { getCurrentUser, listUsers } from './api/moradaApi';
import { useSession } from './hooks/useSession';
import { PortariaMorada } from './components/PortariaMorada';
import { EnderecosMorador } from './components/EnderecosMorador';
import { GestaoMoradores } from './components/GestaoMoradores';
import { FichaMorador } from './components/FichaMorador';
import type { MoradaUser } from './types/morada';
import { useState } from 'react';

export function App() {
  const sessionController = useSession();
  const queryClient = useQueryClient();
  const [selectedUser, setSelectedUser] = useState<MoradaUser | null>(sessionController.session?.user ?? null);

  const currentUserQuery = useQuery({
    queryKey: ['current-user'],
    queryFn: getCurrentUser,
    enabled: Boolean(sessionController.session),
  });

  const usersQuery = useQuery({
    queryKey: ['users'],
    queryFn: listUsers,
    enabled: Boolean(sessionController.session && sessionController.isAdmin),
  });

  if (!sessionController.session) {
    return <PortariaMorada onAuthenticated={sessionController.signIn} />;
  }

  const activeUser = selectedUser ?? currentUserQuery.data ?? sessionController.session.user;
  const canUseDirectory = sessionController.isAdmin;

  function handleSignOut() {
    queryClient.clear();
    setSelectedUser(null);
    sessionController.signOut();
  }

  return (
    <main className="morada-painel-logado">
      <header className="morada-barra-sessao">
        <div className="morada-simbolo-localizacao" aria-hidden="true">
          <MapPin size={24} />
        </div>
        <div className="morada-assinatura-painel">
          <strong>Morada</strong>
          <span>Usuários e endereços com CEP</span>
        </div>
        <button className="morada-botao-sair" type="button" title="Sair" onClick={handleSignOut}>
          <LogOut size={18} />
        </button>
      </header>

      <section className="morada-jornada-cadastros">
        {canUseDirectory && (
          <aside className="morada-diretorio-moradores">
            <div className="morada-titulo-secao">
              <Users size={18} />
              <h2>Usuários</h2>
            </div>
            <GestaoMoradores
              users={usersQuery.data ?? []}
              isLoading={usersQuery.isLoading}
              selectedUserId={activeUser.id}
              onSelectUser={setSelectedUser}
              onUserCreated={(user) => {
                queryClient.invalidateQueries({ queryKey: ['users'] });
                setSelectedUser(user);
              }}
            />
          </aside>
        )}

        <section className="morada-painel-cadastro">
          <FichaMorador user={activeUser} isOwnProfile={activeUser.id === sessionController.session.user.id} />
          <EnderecosMorador user={activeUser} actor={sessionController.session.user} />
        </section>
      </section>
    </main>
  );
}
