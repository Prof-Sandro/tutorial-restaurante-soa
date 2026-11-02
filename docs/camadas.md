# Camadas do restaurante

## Domínio
Representa Mesa, Categoria, Produto, Pedido e ItemPedido.
Contém cálculos como o subtotal de um item.
Não conhece banco de dados, SOAP ou REST.

## Aplicação
Coordena as operações na classe Restaurante.
Exemplo de regra: mesa ocupada não pode abrir outro pedido.
Usa a interface Repositorio para consultar e gravar dados.

## Infraestrutura
Implementa Repositorio na classe JdbcRepositorio.
Executa SQL e controla as conexões com o banco.

## Entradas SOAP e REST
Recebem solicitações, chamam Restaurante e devolvem respostas.
A regra de negócio fica na aplicação.

## Dependências do núcleo
A seta significa: uma parte conhece tipos da outra.

aplicacao -> dominio
infraestrutura -> aplicacao e dominio

A interface Repositorio fica na aplicação.
A implementação JdbcRepositorio fica na infraestrutura.
A aplicação usa a interface sem depender da implementação JDBC.

## Exemplo: abrir pedido
1. SOAP ou REST recebe a solicitação com o identificador da mesa.
2. A aplicação consulta a mesa pela interface Repositorio.
3. Se a mesa estiver ocupada, a aplicação recusa a operação.
4. Se estiver livre, cria o pedido e ocupa a mesa na mesma transação.
5. SOAP ou REST devolve o resultado ao cliente.

As consultas e gravações desses passos são executadas
pela infraestrutura, por meio da interface Repositorio.