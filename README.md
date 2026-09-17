Sistema de Endereços

Sistema para cadastrar usuários e manter os endereços ligados a cada pessoa. A busca do endereço começa pelo CEP, usando a API pública ViaCEP, e passa sempre pelo backend para que a aplicação consiga guardar o resultado em cache.


Estrutura

- backend: Java com Spring Boot, Spring Security, JPA e SQLite.
- frontend: interface em React com TypeScript, Axios e React Query.


O que foi entregue

- Cadastro e login usando CPF e senha.
- Validação de CPF no navegador e também na API.
- Perfil ADMIN para consultar e criar usuários.
- Perfil USER limitado ao próprio cadastro e aos próprios endereços.
- Cadastro, edição, exclusão e marcação de endereço principal.
- Apenas um endereço principal por usuário.
- Troca automática do endereço principal quando o atual é removido.
- Consulta de CEP centralizada no backend, com cache na tabela cep_cache.
- Mensagens de erro tratadas pela API e exibidas no frontend.


Rodando com Docker

docker compose up --build


Depois de subir os containers:

- Frontend: http://localhost:5173
- Backend: http://localhost:8080


O administrador inicial é criado automaticamente:

- Nome: Vinicius Arruda
- CPF: 181.149.677-65
- Senha: admin123


Rodando localmente

Backend:

cd backend
mvn spring-boot:run


Frontend:

cd frontend
npm install
npm run dev


Por padrão o frontend usa a API em http://localhost:8080/api. Para apontar para outra URL:

VITE_API_URL=http://localhost:8080/api npm run dev


A URL do ViaCEP também pode ser alterada no backend:

VIACEP_BASE_URL=http://viacep.com.br/ws mvn spring-boot:run


Testes

cd backend
mvn test


Os testes focam nas regras que eu considerei mais importantes para o desafio: CPF válido e manutenção de apenas um endereço principal por usuário.


Como organizei a solução

- O controller ficou responsável por receber a requisição e devolver a resposta HTTP.
- As regras de cadastro, permissão, CEP e endereço principal ficaram nos services.
- Os repositories ficaram restritos ao acesso ao banco via JPA.
- A autorização foi mantida no backend; o frontend ajuda na navegação, mas não é a barreira de segurança.
- Usei SQLite porque deixa a avaliação local mais simples e ainda mantém um banco relacional de verdade.
- O cache de CEP ficou em tabela para reaproveitar o mesmo endereço entre usuários diferentes.
- Quando o endereço principal é apagado, o sistema promove o endereço mais antigo daquele usuário. Preferi essa regra porque ela é previsível e fácil de explicar.
- O JWT foi assinado com HMAC SHA-256 usando APIs padrão do Java, evitando uma biblioteca extra só para essa parte.


Endpoints principais

- POST /api/auth/register
- POST /api/auth/login
- GET /api/users/me
- GET /api/users
- POST /api/users
- GET /api/users/{userId}/addresses
- POST /api/users/{userId}/addresses
- PUT /api/addresses/{addressId}
- PATCH /api/addresses/{addressId}/primary
- DELETE /api/addresses/{addressId}
- GET /api/ceps/{cep}
