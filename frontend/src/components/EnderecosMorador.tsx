import { FormEvent, useEffect, useMemo, useState } from 'react';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { Edit3, MapPin, Plus, Save, Search, Star, Trash2, X } from 'lucide-react';
import { createAddress, deleteAddress, listAddresses, lookupCep, setPrimaryAddress, updateAddress } from '../api/moradaApi';
import { getApiMessage } from '../api/client';
import { formatCep, onlyDigits } from '../lib/documentNumbers';
import type { MoradaAddress, MoradaAddressDraft, MoradaCepDetails, MoradaUser } from '../types/morada';

type EnderecosMoradorProps = {
  user: MoradaUser;
  actor: MoradaUser;
};

export function EnderecosMorador({ user, actor }: EnderecosMoradorProps) {
  const queryClient = useQueryClient();
  const [addressBeingEdited, setAddressBeingEdited] = useState<MoradaAddress | null>(null);
  const canManage = actor.role === 'ADMIN' || actor.id === user.id;

  const addressesQuery = useQuery({
    queryKey: ['addresses', user.id],
    queryFn: () => listAddresses(user.id),
  });

  const removeMutation = useMutation({
    mutationFn: deleteAddress,
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ['addresses', user.id] }),
  });

  const primaryMutation = useMutation({
    mutationFn: setPrimaryAddress,
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ['addresses', user.id] }),
  });

  const addressSubtitle = (address: MoradaAddress) =>
    [
      address.district,
      `${address.city}/${address.state}`,
      `CEP ${formatCep(address.cep)}`,
    ].join(' - ');

  return (
    <section className="morada-enderecos-morador">
      <div className="morada-titulo-secao">
        <MapPin size={18} />
        <h2>Endereços</h2>
      </div>

      {canManage && (
        <EditorEnderecoMorada
          userId={user.id}
          editingAddress={addressBeingEdited}
          onDone={() => {
            setAddressBeingEdited(null);
            queryClient.invalidateQueries({ queryKey: ['addresses', user.id] });
          }}
          onCancel={() => setAddressBeingEdited(null)}
        />
      )}

      <div className="morada-lista-enderecos" aria-busy={addressesQuery.isLoading}>
        {(addressesQuery.data ?? []).map((address) => (
          <article className="morada-endereco-linha" key={address.id}>
            <div className="morada-endereco-corpo">
              <div className="morada-endereco-cabecalho">
                <strong>{[address.street, address.number].join(', ')}</strong>
                {address.primaryAddress && (
                  <span className="morada-selo-principal">
                    <Star size={14} fill="currentColor" />
                    Principal
                  </span>
                )}
              </div>
              <p>
                {addressSubtitle(address)}
              </p>
              {address.complement && <small>{address.complement}</small>}
            </div>

            {canManage && (
              <div className="morada-controles-endereco">
                {!address.primaryAddress && (
                  <button
                    className="morada-botao-endereco"
                    type="button"
                    title="Definir como principal"
                    onClick={() => primaryMutation.mutate(address.id)}
                  >
                    <Star size={17} />
                  </button>
                )}
                <button className="morada-botao-endereco" type="button" title="Editar endereço" onClick={() => setAddressBeingEdited(address)}>
                  <Edit3 size={17} />
                </button>
                <button
                  className="morada-botao-endereco morada-botao-remover-endereco"
                  type="button"
                  title="Excluir endereço"
                  onClick={() => removeMutation.mutate(address.id)}
                >
                  <Trash2 size={17} />
                </button>
              </div>
            )}
          </article>
        ))}

        {!addressesQuery.isLoading && (addressesQuery.data ?? []).length === 0 && (
          <p className="morada-lista-vazia">Nenhum endereço cadastrado para este usuário.</p>
        )}
      </div>
    </section>
  );
}

type EditorEnderecoMoradaProps = {
  userId: number;
  editingAddress: MoradaAddress | null;
  onDone(): void;
  onCancel(): void;
};

function EditorEnderecoMorada({ userId, editingAddress, onDone, onCancel }: EditorEnderecoMoradaProps) {
  const [cep, setCep] = useState('');
  const [number, setNumber] = useState('');
  const [complement, setComplement] = useState('');
  const [primaryAddress, setPrimaryAddress] = useState(false);
  const [formError, setFormError] = useState('');

  useEffect(() => {
    setCep(editingAddress ? formatCep(editingAddress.cep) : '');
    setNumber(editingAddress?.number ?? '');
    setComplement(editingAddress?.complement ?? '');
    setPrimaryAddress(editingAddress?.primaryAddress ?? false);
    setFormError('');
  }, [editingAddress]);

  const normalizedCep = onlyDigits(cep);
  const cepQuery = useQuery({
    queryKey: ['cep', normalizedCep],
    queryFn: () => lookupCep(normalizedCep),
    enabled: normalizedCep.length === 8,
    staleTime: 1000 * 60 * 60 * 24,
  });

  const preview = useMemo<MoradaCepDetails | null>(() => {
    if (cepQuery.data) {
      return cepQuery.data;
    }
    if (editingAddress && onlyDigits(editingAddress.cep) === normalizedCep) {
      return {
        cep: editingAddress.cep,
        street: editingAddress.street,
        district: editingAddress.district,
        city: editingAddress.city,
        state: editingAddress.state,
      };
    }
    return null;
  }, [cepQuery.data, editingAddress, normalizedCep]);

  const saveMutation = useMutation({
    mutationFn: (addressDraft: MoradaAddressDraft) => {
      if (editingAddress) {
        return updateAddress(editingAddress.id, addressDraft);
      }
      return createAddress(userId, addressDraft);
    },
    onSuccess: () => {
      if (!editingAddress) {
        setCep('');
        setNumber('');
        setComplement('');
        setPrimaryAddress(false);
      }
      onDone();
    },
    onError: (error) => setFormError(getApiMessage(error)),
  });

  function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setFormError('');

    if (normalizedCep.length !== 8) {
      setFormError('Informe um CEP válido.');
      return;
    }
    if (!number.trim()) {
      setFormError('Informe o número do endereço.');
      return;
    }
    if (cepQuery.isFetching) {
      setFormError('Aguarde a consulta do CEP terminar.');
      return;
    }
    if (!preview) {
      setFormError('Consulte um CEP válido antes de salvar.');
      return;
    }

    saveMutation.mutate({
      cep: normalizedCep,
      number,
      complement,
      primaryAddress,
    });
  }

  return (
    <form className="morada-formulario-endereco" onSubmit={handleSubmit}>
      <div className="morada-campos-endereco">
        <label>
          CEP
          <div className="morada-campo-cep">
            <input
              value={cep}
              inputMode="numeric"
              placeholder="00000-000"
              onChange={(event) => setCep(formatCep(event.target.value))}
              required
            />
            <Search size={17} />
          </div>
        </label>
        <label>
          Número
          <input value={number} onChange={(event) => setNumber(event.target.value)} required />
        </label>
        <label>
          Complemento
          <input value={complement} onChange={(event) => setComplement(event.target.value)} />
        </label>
      </div>

      <div className="morada-previa-cep">
        {cepQuery.isFetching && <span>Consultando CEP...</span>}
        {cepQuery.isError && <span className="morada-aviso-formulario">CEP não encontrado.</span>}
        {preview && (
          <span>{[preview.street, preview.district, `${preview.city}/${preview.state}`].join(', ')}</span>
        )}
      </div>

      <div className="morada-rodape-endereco">
        <label className="morada-marca-principal">
          <input type="checkbox" checked={primaryAddress} onChange={(event) => setPrimaryAddress(event.target.checked)} />
          Endereço principal
        </label>

        {formError && <p className="morada-aviso-formulario">{formError}</p>}

        <div className="morada-acoes-endereco">
          {editingAddress && (
            <button className="morada-botao-cancelar-endereco" type="button" onClick={onCancel}>
              <X size={16} />
              Cancelar
            </button>
          )}
          <button className="morada-botao-salvar-endereco" type="submit" disabled={saveMutation.isPending || cepQuery.isFetching}>
            {editingAddress ? <Save size={16} /> : <Plus size={16} />}
            {saveMutation.isPending ? 'Salvando...' : editingAddress ? 'Salvar endereço' : 'Adicionar endereço'}
          </button>
        </div>
      </div>
    </form>
  );
}
