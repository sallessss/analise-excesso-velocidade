# Decisões do projeto

## Situações não cobertas pelo enunciado

- **Placas em letras minúsculas:** normalizei para maiúsculas antes do agrupamento, evitando que capitalizações diferentes criem veículos distintos.
- **Espaços nos campos:** removi espaços no início e no fim antes de validar e interpretar os valores.
- **Decimal com vírgula:** aceitei vírgula como separador decimal, pois o CSV utiliza ponto e vírgula entre os campos.
- **Limite igual a zero ou negativo:** considerei a linha inválida. Além de não representar um limite coerente, zero impediria o cálculo percentual.
- **Campos vazios, data inválida, número inválido, velocidade negativa, quantidade incorreta de campos e linha vazia:** considerei cada ocorrência uma linha inválida, preservando seu número e continuando o processamento.
- **Leituras com a mesma placa e data/hora:** preservei todas e usei o número original da linha como segundo critério de ordenação. Assim, o resultado permanece determinístico e respeita a ordem do arquivo em caso de empate.
- **Episódio com uma única leitura:** defini início e fim iguais e duração de zero segundos.
- **Virada de dia:** utilizei data e hora completas com `LocalDateTime`, permitindo calcular corretamente episódios que atravessam a meia-noite.
- **Arredondamento do percentual:** utilizei uma casa decimal com `RoundingMode.HALF_UP`. A gravidade é classificada a partir desse valor arredondado, que é o mesmo apresentado na resposta.
- **Cabeçalho:** exigi exatamente `placa;dataHora;velocidadeKmh;limiteKmh`. Arquivo vazio ou cabeçalho diferente é rejeitado por inteiro com HTTP 400.
- **Ordenação dos episódios:** ordenei por data/hora inicial e, em caso de empate, pela placa, para manter uma resposta determinística.

## Parte mais difícil

A parte mais delicada foi formar os episódios sem perder as regras de fronteira. Primeiro agrupei as leituras por placa e as ordenei por data/hora e número da linha original. Depois percorri cada grupo, encerrando o episódio somente ao encontrar uma leitura sem excesso ou um intervalo estritamente maior que cinco minutos. Mantive o cálculo do maior percentual separado da maior velocidade porque os limites podem variar entre leituras.

Os testes automatizados cobrem especialmente os limites exatos de 5 km/h, 5 minutos, 20% e 50%, a ordenação fora de ordem, a virada de dia e os desempates do ranking.

## O que eu faria diferente com mais tempo

- Adicionaria testes de ponta a ponta no navegador para automatizar upload, filtro e apresentação dos resultados em diferentes tamanhos de tela.
- Acrescentaria verificações de acessibilidade automatizadas e navegação completa por teclado.
- Configuraria health checks no Docker Compose e uma etapa de integração contínua para executar os testes a cada alteração.
- Avaliaria limites configuráveis de tamanho e quantidade de linhas para proteger o processamento em memória com arquivos muito grandes.
