# Shadow Katana — Mod Fabric (Minecraft Java)  |  ARQUIVO DE PASSAGEM PARA OUTRA IA

## Pedido do usuário (português)
- Katana 3D toda preta, bem sombria, modelada para o Blockbench.
- Um botão (tecla): o personagem saca a espada das costas; guardada, ela FICA SEMPRE nas costas.
- Um menu só da espada para AUMENTAR o dano da espada e o dano dos poderes.
- Poderes escolhidos: **Sombra** = teleporte + invisibilidade.
- Plataforma: **Fabric** (Java). Versão exata do Minecraft ainda NÃO confirmada -> assumir 1.21.x e perguntar ao usuário se precisar.
- Trabalho feito em etapas; este arquivo é o controle de progresso.

## Etapas
- [x] **Etapa 1 — Modelo 3D + textura**: `tools/generate_model.py` gera tudo (rode de novo para ajustar).
      Saídas: `shadow_katana.bbmodel` (abrir no Blockbench), `assets/shadowkatana/models/item/shadow_katana.json`,
      `assets/shadowkatana/textures/item/shadow_katana.png` (32x32).
- [x] **Etapa 2 — Esqueleto do mod Fabric (1.21.8)** FEITA (NAO COMPILADA: sem internet no ambiente; testar com `./gradlew runClient` e corrigir erros).: build.gradle, gradle.properties, fabric.mod.json, classe ShadowKatanaMod,
      registrar item `shadowkatana:shadow_katana` (SwordItem custom), lang pt_br/en_us, item definition/model.
- [x] **Etapa 3 — Espada nas costas + tecla R + animação** FEITA (NÃO COMPILADA, ver notas abaixo).
      (descrição original:): keybind (padrão R) -> pacote de rede cliente->servidor;
      estado `drawn` (boolean) salvo por jogador (attachment/data). Renderizador (FeatureRenderer no PlayerEntityRenderer)
      desenha o modelo nas costas inclinado ~45° quando `drawn == false`; quando `drawn == true` o item está na mão
      e nada é desenhado nas costas. Espada deve aparecer nas costas mesmo sem estar no inventário ativo (ficar no inventário/slot dedicado).
- [x] **Etapa 4 — Menu de upgrade (tecla K)** FEITA (NÃO COMPILADA).
      (descrição original:): Screen no cliente com botões para subir nível de: dano da espada,
      dano do Teleporte, dano da Sombra (dano ao chegar/ao sair da invisibilidade). Níveis salvos por jogador; custo em XP ou itens (definir).
      Sincronizar cliente <-> servidor por pacotes.
- [x] **Etapa 5 — Poderes de Sombra** FEITA (NÃO COMPILADA).
      (descrição original:): tecla/ação 1 = Teleporte curto na direção do olhar (raycast, ~10 blocos, dano em área ao chegar);
      tecla/ação 2 = Invisibilidade (efeito INVISIBILITY por X segundos, próximo golpe causa dano bônus). Cooldowns + partículas pretas/roxas.
- [x] **Etapa 6 — Polimento** FEITA (receita + HUD; sons próprios ficaram de fora).
      (descrição original:): som, partículas, receita de craft, teste, empacotar `.jar`.

## Decisões / notas
- Modelo: lâmina vertical no eixo Y, cabo embaixo (y -3.9 até 5), ponta em y≈29.6. Ajustar `display` no Blockbench (aba Display) se a posição na mão ficar torta.
- Cores: preto (#070709 a #1f1f26) com leve brilho frio no fio da lâmina. Nada colorido além de partículas dos poderes.
- Se o Blockbench reclamar do `.bbmodel`: File > Open Model e abra o `.json` de `assets/.../models/item/`.
- Pacote base sugerido: `com.shadowkatana`. Mod id: `shadowkatana`.

## Estado atual
TODAS AS 6 ETAPAS ESCRITAS. NADA FOI COMPILADO AINDA (o ambiente onde o código foi escrito não tinha internet).
PRÓXIMO PASSO PARA QUALQUER IA/PESSOA: compilar (`./gradlew build`), corrigir erros de API 1.21.8, rodar `./gradlew runClient` e seguir o checklist do README.md.

### Etapa 6
- Receita (shaped, diagonal): 2 netherite ingot + 2 obsidian + 1 stick -> `data/shadowkatana/recipe/shadow_katana.json` (+ advancement para liberar no livro de receitas).
- HUD de recarga: `client/ShadowHud.java` usando `HudElementRegistry.addLast` (Fabric API 0.129). Se não existir nessa versão do Fabric API, trocar por `HudRenderCallback.EVENT`. Recargas `TELEPORT_READY_AT` / `SHADOW_READY_AT` agora sincronizadas com o dono.
- Possível ajuste de compilação extra: assinatura `HudElement` (`(DrawContext, RenderTickCounter)`).

### Como a Etapa 5 funciona
- Decisões do usuário: **Z = teleporte, X = invisibilidade**, invisibilidade dura **20 s**.
- Teclas no cliente -> `PowerPayload(0|1)` -> `ShadowPowers.use` (servidor). Exige fase DRAWN (katana na mão).
- Teleporte: raycast até 12 blocos na direção do olhar, recua até achar espaço livre, causa `KatanaUpgrades.teleportDamage(nível)` a seres vivos no caminho/destino (raio 2.5), partículas SQUID_INK/PORTAL, som de enderman, cooldown 3 s. Zera fallDistance.
- Invisibilidade: efeito INVISIBILITY 400 ticks + `STRIKE_ARMED=true` (golpe sombrio). Enquanto armado e segurando a katana, `KatanaUpgrades.tickBonus` soma `shadowStrikeBonus(nível)` ao ATTACK_DAMAGE; o primeiro golpe corpo a corpo acertado consome o golpe (`ServerLivingEntityEvents.AFTER_DAMAGE`). A invisibilidade continua até acabar os 20 s. Cooldown 40 s desde o uso. A katana nas costas some (renderer checa `state.invisible`).
- Constantes de balanceamento: topo de `ShadowPowers.java` (alcance, cooldowns, duração) e `KatanaUpgrades.java` (dano por nível).
- Riscos de compilação Etapa 5: `Entity.raycast(double,float,boolean)`, `World.isSpaceEmpty(Entity,Box)`, `Entity.damage(ServerWorld,DamageSource,float)`, `SoundEvents.ENTITY_ENDERMAN_TELEPORT` (pode ser RegistryEntry -> usar `.value()`), `ServerWorld.spawnParticles(...)`, `Entity.requestTeleport(double,double,double)`, `p.fallDistance` (double/float), `StatusEffectInstance` construtor de 6 args, `ServerLivingEntityEvents.AFTER_DAMAGE` (5 args).
- Limitação: armadura e item na mão continuam visíveis com invisibilidade (comportamento do jogo base).
- Etapa 6 sugerida: receita de craft em `data/shadowkatana/recipe/shadow_katana.json`, sons próprios, HUD com recarga, partículas de rastro, testes e `./gradlew build`.

### Como a Etapa 4 funciona
- Decisões do usuário: upgrades pagos em NÍVEIS DE XP; nível máximo **50** por upgrade; 3 upgrades: dano da katana, dano do teleporte, dano do golpe sombrio.
- Tecla **K** abre `KatanaUpgradeScreen` (client). Botões enviam `UpgradePayload(type)` -> `KatanaUpgrades.tryUpgrade` (servidor valida XP, cobra, sobe o nível).
- Níveis: attachments persistentes `ModAttachments.SWORD_LEVEL / TELEPORT_LEVEL / SHADOW_LEVEL` (copyOnDeath, sync só com o dono).
- Balanceamento (tudo em `KatanaUpgrades.java`): custo = 1 + nível/10 níveis de XP; bônus de espada = +0.5 dano por nível (aplicado como modificador temporário de ATTACK_DAMAGE enquanto segura a katana, em `tickBonus`);
  `teleportDamage(level)` = 2 + 0.5*nível e `shadowStrikeBonus(level)` = 2 + 0.75*nível **JÁ EXISTEM mas ainda não são usados: a Etapa 5 deve chamá-los**.
- Criativo não paga XP.
- Riscos de compilação Etapa 4: `DrawContext.drawBorder/drawCenteredTextWithShadow/drawTextWithShadow`, `Screen.renderBackground(DrawContext,int,int,float)`, `KeyBinding.matchesKey(int,int)`, `EntityAttributes.ATTACK_DAMAGE`, `EntityAttributeInstance.getModifier(Identifier)/removeModifier(Identifier)/addTemporaryModifier`, `PacketCodec.tuple`, `AttachmentSyncPredicate.targetOnly()`, `SoundEvents.ENTITY_PLAYER_LEVELUP`.

### Como a Etapa 3 funciona
- Tecla **R** (client `ShadowKatanaClient`) -> payload `ToggleDrawPayload` -> `KatanaLogic.onToggle` (servidor).
- Fases em `KatanaPhase` (NONE/SHEATHED/DRAWING/DRAWN/SHEATHING), guardadas no jogador via Fabric Attachment `ModAttachments.PHASE` (sincronizada com todos).
- Sacar = após DRAW_TICKS o servidor seleciona o slot da hotbar onde está a katana. Guardar = volta ao slot anterior (`PREV_SLOT`). A katana precisa estar na HOTBAR.
- Nas costas: `BackKatanaFeatureRenderer` (FeatureRenderer do jogador) desenha o item enquanto fase != NONE/DRAWN, com animação interpolada entre pose das costas e pose da mão.
  Pegamos a entidade por `state.id` (render state 1.21.8 não tem a entidade) e a fase via attachment.
- TODOS os números da pose/animação estão em `client/render/KatanaPose.java` (ajustar olhando no jogo: se a katana aparecer torta, mudar BACK_ROLL/BACK_X/Y/Z; se girar para o lado errado, negar o ângulo).
- Invisibilidade (Etapa 5): o renderer já esconde a katana se `state.invisible`.

### Pontos de risco (não compilado!) — se der erro de compilação, olhar primeiro:
1. `ItemModelManager.updateForNonLivingEntity(...)` em BackKatanaFeatureRenderer (nome/assinatura pode variar na 1.21.8).
2. `PlayerInventory.getSelectedSlot()/setSelectedSlot(int)` em KatanaLogic (alternativa: campo `selectedSlot`).
3. `ModelTransformationMode.HEAD` (pode ser `ItemDisplayContext` em outras versões).
4. `FeatureRenderer.render(...)` com `VertexConsumerProvider` (1.21.8) — em 1.21.9+ muda para OrderedRenderCommandQueue.
5. `AttachmentRegistry.create(...).syncWith(PacketCodecs.VAR_INT, AttachmentSyncPredicate.all())`.
6. `KeyBinding` com categoria String (1.21.9+ usa KeyBinding.Category).
Ideia futura (Etapa 3b, opcional): levantar o braço direito do jogador durante o saque via Mixin em PlayerEntityModel.setAngles (hoje a katana "voa" até a mão sem o braço levantar).
Minecraft **1.21.8**, Fabric, mappings Yarn, Java 21, Loom 1.10-SNAPSHOT.
Versões em gradle.properties (yarn 1.21.8+build.1, loader 0.16.14, fabric api 0.129.0+1.21.8) vieram de memória: CONFERIR em fabricmc.net/develop.
Arquivos: ShadowKatanaMod (main), ModItems (registro, espada NETHERITE, dano base 3, velocidade -2.2), ShadowKatanaItem (vazio, para etapas 4/5),
ShadowKatanaClient (vazio, para keybinds/renderer da etapa 3).
Item model 1.21.4+ usa assets/shadowkatana/items/shadow_katana.json apontando para models/item/shadow_katana.json.
Etapa 3 (dicas): API de render mudou em 1.21.2+ (render states: PlayerEntityRenderState, FeatureRenderer.render(matrices, queue, light, state, yaw, pitch)); conferir docs.fabricmc.net/1.21.8.
Rede: usar PayloadTypeRegistry + ServerPlayNetworking (CustomPayload com CODEC). Estado por jogador: Fabric Data Attachments (AttachmentRegistry.createPersistent).

### Etapa 7 — Animacoes + sons (NAO COMPILADA)
- Sacar (14 ticks) / guardar (12 ticks): braco direito levanta ate o cabo, katana sai das costas so depois (KatanaAnim.REACH), corte para frente. Guardar = mesmo movimento ao contrario.
- Braco: mixin client `BipedEntityModelMixin` (injeta no TAIL de BipedEntityModel.setAngles, require=0) + tabela ARM em `KatanaAnim.java`. Katana: `KatanaPose.java`.
- Sons proprios (`ModSounds`, `sounds.json`, ogg gerados por `tools/generate_sounds.py`): katana_draw (tick 4 do saque), katana_swish (tick 9 e ao guardar), katana_sheathe (tick 9 do guardar). Tempos em `KatanaLogic.tick`.
- Build no GitHub: `.github/workflows/build.yml` (Gradle 8.14 + Java 21; projeto sem gradlew). Jar em Actions > Artifacts > shadowkatana-mod.
