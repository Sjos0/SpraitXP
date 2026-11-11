# PRD de Design: SpriteXP (direção retrô Windows XP)

Versão: 1.0 | Data: 2026-11-11

---

## 1. Direção criativa
"Oficina de pixel art em estilo XP". A interface lembra o Windows XP (janelas com barra azul, botões em relevo, barra de tarefas verde) mas é limpa e usável com o dedo. Nostalgia com clareza, não cópia.

Palavras-chave: nostálgico, amigável, organizado, leve, divertido.

## 2. Princípios de design
1. **Reconhecível:** quem já usou XP sente familiaridade na primeira abertura
2. **Touch primeiro:** tudo que o XP fazia com mouse vira toque, arrastar e gesto
3. **Leve visualmente:** pouca textura pesada, gradientes simples, ícones pequenos
4. **Ferramenta antes de enfeite:** o conteúdo (a imagem do usuário) é o protagonista
5. **Sem marca alheia:** nada de logo, nome ou ícones da Microsoft

## 3. Paleta de cores

| Papel | Nome | Hex |
|---|---|---|
| Barra de título (início) | Luna Azul Escuro | #0A246A |
| Barra de título (meio) | Luna Azul | #245EDC |
| Barra de título (fim) | Luna Azul Claro | #3A93FF |
| Fundo de janela | Bege XP | #ECE9D8 |
| Fundo de área de trabalho | Verde colina | #5AAE3C |
| Céu do fundo | Azul céu | #6FA8F5 |
| Botão Iniciar / ação principal | Verde Start | #3C9A3C |
| Botão fechar / alerta | Laranja XP | #E0572A |
| Painel escuro (área de pixel art) | Cinza grafite | #2B2B2B |
| Destaque de seleção | Amarelo | #FFE066 |
| Texto principal | Preto | #000000 |
| Texto secundário | Cinza escuro | #404040 |
| Fundo de tela neutro (paleta de pixel) | Cinza XP | #D4D0C8 |

Regra de contraste: texto sobre fundos azuis sempre branco. Texto sobre bege sempre preto.

## 4. Tipografia

| Uso | Família sugerida | Tamanho | Peso |
|---|---|---|---|
| Títulos de janela | Pixelify Sans ou VT323 | 18 sp | Bold |
| Títulos de tela | Pixelify Sans | 22 sp | Bold |
| Corpo de texto | Noto Sans ou Open Sans | 14 sp | Regular |
| Legendas e rótulos | Noto Sans | 12 sp | Regular |
| Números (FPS, contagens) | VT323 | 20 sp | Regular |

Observação: a fonte Tahoma, clássica do XP, é de propriedade da Microsoft. Use uma fonte livre parecida (ex.: Noto Sans, Open Sans) para evitar problemas de licença.

## 5. Componentes

### 5.1 Janela
- Barra de título em gradiente azul com ícone pixel à esquerda e título em branco
- Botões de janela no canto direito: minimizar, maximizar e fechar (apenas fechar é obrigatório no mobile)
- Cantos arredondados de 6 dp no topo
- Borda externa de 2 dp em azul escuro
- Corpo em bege com borda interna em cinza

### 5.2 Botões
- **Padrão:** bege com borda em relevo (clara em cima e à esquerda, escura embaixo e à direita)
- **Pressionado:** relevo invertido, leve deslocamento de 1 dp
- **Primário:** verde Start com texto branco
- **Perigo:** laranja XP (ex.: apagar projeto)
- **Desabilitado:** texto cinza com contorno pontilhado
- Altura mínima 48 dp

### 5.3 Barra de navegação inferior (estilo barra de tarefas)
- Faixa azul de 56 dp
- Botão Iniciar verde à esquerda, com logo pixel próprio (não o logo da Microsoft)
- Atalhos das telas abertas à direita, como botões de tarefa
- Relógio à direita (opcional)

### 5.4 Menu Iniciar (gaveta)
- Painel vertical que sobe da barra inferior
- Cabeçalho azul com nome do usuário (ou "Artista")
- Lista de ferramentas com ícone e descrição curta
- Rodapé com "Configurações" e "Sair"

### 5.5 Área de trabalho (tela inicial)
- Fundo de colina verde e céu azul em estilo simples e vetorial (sem reproduzir fotos de fundo conhecidas)
- Ícones de atalho em pixel art de 32 px, com rótulo branco sombreado
- Toque duplo ou toque simples (configurável) para abrir

### 5.6 Barra de progresso
- Blocos verdes segmentados que aparecem conforme o processamento avança
- Texto com porcentagem em VT323
- Animação suave de preenchimento (sem efeito pesado)

### 5.7 Balões de dica
- Balão amarelo claro com borda preta e ponta, aparecendo ao tocar e segurar um controle
- Fecha ao tocar fora

### 5.8 Controles deslizantes
- Trilho em cinza com trecho preenchido em azul
- Cursor redondo em verde com borda
- Valor atual aparece ao lado em fonte VT323

### 5.9 Linha de frames (timeline)
- Grade de miniaturas com borda fina
- Frame selecionado com contorno amarelo tracejado
- Arrastar para reordenar
- Número do frame no canto inferior

### 5.10 Caixas de diálogo
- Janela pequena no centro, com ícone de alerta, texto e dois botões (ex.: OK e Cancelar)
- Escurece levemente o fundo

## 6. Layout mobile

### Orientação
- **Retrato:** padrão para todas as telas
- **Paisagem:** timeline e analisador de sprite sheet

### Estrutura de tela
1. Barra de título da janela (topo, 48 dp)
2. Conteúdo principal (área de trabalho da janela)
3. Painel de controles (rolável ou gaveta inferior)
4. Barra de navegação (fixa no fundo)

### Grid
- Margens de 12 dp nas laterais
- Espaçamento múltiplo de 4 dp (4, 8, 12, 16, 24)
- Cantos de componentes em 4 dp

## 7. Telas do MVP

| # | Tela | Conteúdo principal |
|---|---|---|
| 1 | Splash | Logo pixel e barra de progresso XP |
| 2 | Área de trabalho | Ícones das ferramentas e atalhos recentes |
| 3 | Menu Iniciar | Gaveta com ferramentas e configurações |
| 4 | Conversor de imagem | Comparativo original e pixel art, controles |
| 5 | Vídeo para sprite sheet | Pré-visualização, intervalo, timeline de frames |
| 6 | Analisador de sprite sheet | Grade detectada, seleção, prévia de animação |
| 7 | Paleta e dithering | Presets, cores ativas, modo de dithering |
| 8 | Exportação | Formatos, opções, compartilhar |
| 9 | Projetos | Lista de projetos salvos e sprite sets |
| 10 | Configurações | Tema, idioma, sons, sobre |

## 8. Animações e sons
- Animações curtas (150 a 250 ms), sem efeito de parallax
- Abertura de janela: escala de 0.9 para 1.0 com fade
- Sons opcionais: "chime" de 8 bits para concluir exportação e erro suave para falha
- Não usar sons de marcas registradas

## 9. Ícones
- Estilo pixel art em grade de 16x16 ou 32x32
- Contorno preto de 1 px
- Sombreado em três tons da mesma cor base
- Conjunto mínimo: converter, vídeo, analisar, paleta, exportar, pasta, configurações, ajuda

## 10. Acessibilidade
- Contraste mínimo de 4.5:1 para texto corpo
- Alvos de toque de no mínimo 48 dp
- Suporte ao tamanho de fonte do sistema
- Modo alto contraste opcional (bordas mais grossas, sem gradiente)
- Descrição de conteúdo (content description) em todos os ícones

## 11. Modo escuro
- Janela em cinza escuro (#3C3C3C) com borda clara
- Barra de título mantém o azul, só mais escuro
- Texto claro sobre fundo escuro

## 12. O que não fazer
- Não usar logo, nome, ícones ou sons da Microsoft ou do Windows
- Não reproduzir a foto de fundo "Bliss" ou qualquer imagem protegida
- Não usar a fonte Tahoma sem licença
- Não criar animações pesadas ou efeitos de vidro/blur em excesso
- Não colocar ícones de pixel pequenos demais para toque (abaixo de 24 px na tela)
- Não deixar texto sobre gradiente com baixo contraste

## 13. Checklist de aprovação visual
- [ ] Reconhecível como "XP", mas sem marca alheia
- [ ] Todos os botões com 48 dp ou mais
- [ ] Contraste validado nas cores principais
- [ ] Telas 1 a 10 geradas e revisadas
- [ ] Modo escuro validado
- [ ] Ícones consistentes em uma mesma grade
