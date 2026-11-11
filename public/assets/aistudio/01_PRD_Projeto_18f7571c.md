# PRD do Projeto: SpriteXP

Versão: 1.0 | Data: 2026-11-11 | Responsável: FX

---

## 1. Visão do produto
Um app Android leve e rápido para criar sprites de jogos 2D. O usuário traz uma imagem pronta ou um vídeo e o app devolve pixel art, sprite sheets e arquivos prontos para engine, sem precisar de ferramentas de desktop.

## 2. Problema
- Apps de pixel art no PC são poderosos, mas não existem em versões mobile equivalentes e leves.
- Criar sprite a partir de vídeo (ex.: gravar um personagem se movendo) é trabalhoso: extrair frames, remover fundo, reduzir resolução, limitar cores e montar a grade.
- Ferramentas pesadas consomem bateria e memória em celulares intermediários.

## 3. Público-alvo
- Dev indie de jogos 2D que prototipa no celular
- Artistas de pixel art que querem converter referências rapidamente
- Entusiastas de retrogames que querem criar sprites no estilo clássico
- Usuários de jogos de sprite (ex.: quem monta mods)

## 4. Objetivos e métricas

| Objetivo | Métrica de sucesso |
|---|---|
| Leveza | APK abaixo de 25 MB no MVP |
| Velocidade de abertura | Abrir o app em menos de 2 s em aparelho médio |
| Conversão rápida | Imagem 256x256 para pixel art 64x64 em menos de 300 ms |
| Vídeo para sprite sheet | Vídeo de 10 s (30 fps) processado em menos de 30 s |
| Uso offline | 100% das funções principais sem internet |
| Privacidade | Nenhum arquivo enviado para servidor por padrão |

## 5. Escopo

### MVP (versão 1.0)
1. Conversor de imagem para pixel art
2. Vídeo para sprite sheet (extração de frames)
3. Remoção de fundo por chroma key (cor de fundo escolhida)
4. Ajustes de resolução alvo, escala e contraste/brilho
5. Paletas: automática (extração) e predefinidas (PICO-8, SWEETIE-16, Endesga 32, Game Boy)
6. Dithering: nenhum, ordenado e Floyd-Steinberg
7. Analisador de sprite sheet (detectar grade, selecionar frames, pré-visualizar animação)
8. Exportação: PNG de sprite sheet, ZIP de PNGs, JSON de metadados
9. Projetos salvos (sprite sets) e fila de arquivos
10. Prévia de animação (play/pause, FPS, início e fim)

### Versão 1.1
- Editor pixel básico (lápis, borracha, balde, conta-gotas, desfazer)
- Processamento em lote (Automator)
- Código de integração pronto para engines (Phaser, Godot, Unity)
- Redimensionamento nearest-neighbor
- Gerador de paleta a partir de qualquer imagem

### Versão 2.0 (ideias)
- Exportar para GIF
- Importar de galeria e de câmera
- Modo paisagem para timeline
- Pacotes de paleta compartilháveis
- Widget de acesso rápido

### Fora do escopo (por enquanto)
- Geração de sprite do zero com IA generativa (o foco é converter o que já existe)
- Contas de usuário e nuvem obrigatória
- Edição de vídeo completa

## 6. Funcionalidades detalhadas

### Módulo A: Conversor de imagem para pixel art
- Entrada: galeria ou câmera (PNG, JPG, WEBP)
- Chroma key: escolher cor de fundo com conta-gotas, ajustar tolerância (0 a 100), opção de processar em resolução total
- Realce de bordas: intensidade de 0 a 200%
- Contraste e brilho: controles deslizantes
- Resolução alvo: 16, 32, 48, 64, 96, 128, 256 (com trava de proporção)
- Escala de exibição: 1x, 2x, 4x, 8x, 16x (só exibição e exportação; não altera a resolução de pixel)
- Paleta: automática (máx. de cores configurável) ou predefinida
- Dithering: nenhum, ordenado, Floyd-Steinberg
- Comparação lado a lado: original e pixel art
- Pré-visualização em tempo real com reprocessamento ao mover controles (com debounce)

### Módulo B: Vídeo para sprite sheet
- Entrada: vídeo da galeria ou gravado na hora (MP4, MOV, WEBM)
- Fila de vídeos (múltiplos arquivos)
- Extração de frames com intervalo configurável
- Recorte do intervalo útil (início e fim)
- Aplicação do mesmo pipeline do Módulo A em cada frame (chroma key, paleta, resolução)
- Linha de frames (timeline) com seleção, remoção e reordenação
- Renderizar e tocar: gera a animação completa e exibe em loop
- Saída: sprite sheet, ZIP de PNGs e JSON de frames

### Módulo C: Analisador de sprite sheet
- Carregar uma ou mais sprite sheets
- Detecção automática de grade ou entrada manual de colunas e linhas
- Grade visual sobreposta com numeração dos frames
- Seleção de frames por clique, faixa (início/fim) ou toque longo
- Pré-visualização de animação: FPS, início, fim, play/pause
- Exportação: frames selecionados como sprite sheet, ZIP ou JSON
- Lista de arquivos com ativar/desativar por arquivo
- Sprite sets: agrupar sheets de um mesmo personagem

### Módulo D: Paletas
- Extração automática de cores (até 8, 16, 32 ou 64 cores)
- Predefinidas: PICO-8 (16), SWEETIE-16, Endesga 32, Game Boy (4)
- Paleta personalizada: salvar e nomear
- Prévia da paleta ativa com contagem de cores

### Módulo E: Exportação
- Sprite sheet PNG (com grade de frames configurável)
- ZIP de PNGs individuais
- JSON de metadados (largura, altura, colunas, frames, FPS, nomes)
- Compartilhar via sistema Android

### Módulo F: Ferramentas extras (versão 1.1 em diante)
- Editor pixel básico
- Automator: aplicar um preset a vários arquivos
- Prévia de sprite em tamanho real (1x) em uma tela de teste

## 7. Fluxos de usuário

**Fluxo 1: Imagem para pixel art (principal)**
1. Abre o app e toca em "Nova imagem"
2. Escolhe a imagem da galeria
3. Define cor de fundo (chroma key), se houver
4. Escolhe resolução alvo e paleta
5. Confere o resultado no comparativo
6. Exporta o PNG ou o ZIP

**Fluxo 2: Vídeo para sprite sheet**
1. Toca em "Novo vídeo" e escolhe o arquivo
2. Ajusta início e fim, e a taxa de frames
3. Aplica chroma key, resolução e paleta
4. Toca em "Renderizar e tocar" para ver a animação
5. Seleciona os frames desejados na linha de tempo
6. Exporta a sprite sheet e o JSON

**Fluxo 3: Análise de sprite sheet existente**
1. Carrega a sheet
2. App detecta a grade (ou o usuário informa colunas e linhas)
3. Usuário seleciona frames e ajusta FPS
4. Pré-visualiza a animação e exporta

## 8. Requisitos não funcionais

### Desempenho
- Processamento de imagem e vídeo fora da thread principal (worker/coroutine)
- Limite de resolução de entrada com redução automática (ex.: 1080p para processamento)
- Cache de frames para não reprocessar ao trocar só a visualização
- Liberação de memória de bitmaps após exportação
- Interface mantém 60 fps durante a navegação, mesmo com processamento em segundo plano

### Leveza
- APK alvo abaixo de 25 MB
- Sem bibliotecas pesadas de IA no MVP
- Imagens de interface em formato vetorial ou pixel art pequeno
- Sem SDKs de anúncio ou analytics pesados

### Privacidade
- Processamento 100% local
- Permissões mínimas (acesso à galeria apenas quando necessário)
- Sem login obrigatório

### Compatibilidade
- Android 8.0 ou superior (ajustar após definir o público)
- Telas de 5 a 7 polegadas, portrait como padrão e landscape na timeline

### Acessibilidade
- Alvos de toque de no mínimo 48 dp
- Contraste mínimo WCAG AA
- Textos com escala do sistema

## 9. Arquitetura sugerida (independente da plataforma)
- **Camada de UI:** telas com estado centralizado (um estado por projeto)
- **Camada de pipeline:** etapas encadeadas e reexecutáveis: entrada, chroma key, realce, redução de resolução, dithering, mapeamento de paleta, saída
- **Camada de mídia:** extração de frames de vídeo e leitura/escrita de imagens
- **Camada de armazenamento:** projetos, paletas e sprite sets em banco local leve
- **Camada de exportação:** PNG, ZIP e JSON

Regra de ouro: cada etapa do pipeline é pura (mesma entrada gera mesma saída), o que permite cache e testes.

## 10. Riscos e mitigações

| Risco | Impacto | Mitigação |
|---|---|---|
| Vídeo longo travar o aparelho | Alto | Limite de duração no MVP, processamento em lote com progresso |
| Chroma key ruim em fundos irregulares | Médio | Tolerância ajustável e pré-visualização |
| Pixel art com cores demais | Médio | Paleta automática com limite e presets |
| Uso de memória alto em vídeos | Alto | Processar frame a frame, descartar bitmaps |
| Confusão com marca Sorceress | Médio | Nome e identidade próprios, sem copiar elementos |

## 11. Critérios de aceite do MVP
- [ ] Converter imagem PNG em pixel art 64x64 com paleta de 16 cores
- [ ] Remover fundo verde com chroma key e ver resultado em tempo real
- [ ] Converter vídeo de 10 s em sprite sheet e tocar a animação
- [ ] Carregar sprite sheet, detectar grade e exportar frames selecionados
- [ ] Exportar PNG, ZIP e JSON
- [ ] Salvar e reabrir um projeto
- [ ] Funcionar sem internet
- [ ] APK abaixo de 25 MB

## 12. Roadmap

| Fase | Entregas | Duração estimada |
|---|---|---|
| 0. Definição | PRDs, nome, telas aprovadas | 1 semana |
| 1. Base | Projeto, navegação, tema XP, armazenamento | 1 a 2 semanas |
| 2. Imagem | Módulo A completo | 2 semanas |
| 3. Vídeo | Módulo B completo | 2 semanas |
| 4. Analisador | Módulo C completo | 1 a 2 semanas |
| 5. Exportação e polimento | Módulo E, testes, otimização | 1 semana |
| 6. MVP | Beta fechado e ajustes | 1 semana |

## 13. Glossário
- **Sprite:** imagem de personagem ou objeto usada em jogos 2D
- **Sprite sheet:** imagem única com vários frames em grade
- **Frame:** uma imagem de uma animação
- **Chroma key:** remoção de uma cor de fundo (ex.: verde)
- **Dithering:** técnica de pontilhado para simular mais cores com menos cores
- **Paleta:** conjunto limitado de cores usadas no pixel art
- **Nearest-neighbor:** redimensionamento que preserva pixels nítidos
