import { useMemo, useState } from 'react';
import { moradaSessionStorage } from '../api/client';
import type { MoradaSession } from '../types/morada';

export function useSession() {
  const [session, setSession] = useState<MoradaSession | null>(() => moradaSessionStorage.load());

  return useMemo(
    () => ({
      session,
      isAdmin: session?.user.role === 'ADMIN',
      signIn(nextSession: MoradaSession) {
        moradaSessionStorage.save(nextSession);
        setSession(nextSession);
      },
      signOut() {
        moradaSessionStorage.clear();
        setSession(null);
      },
    }),
    [session],
  );
}
