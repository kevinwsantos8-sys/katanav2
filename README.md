# Shadow Katana (Fabric 1.21.8)

## Como rodar
Requisitos: JDK 21 e IntelliJ IDEA (Community serve).

Caminho mais facil (recomendado, pois eu nao tinha internet para baixar o gradle wrapper):
1. Va em https://fabricmc.net/develop/template , escolha 1.21.8, nome do mod `shadowkatana`, pacote `com.shadowkatana`, baixe e extraia.
2. Copie para dentro dessa pasta nova: a pasta `src/` e `tools/`, e confira o `gradle.properties`: as versoes de yarn/loader/fabric_api devem ser as que o gerador mostrar (use as dele se forem diferentes).
3. Abra no IntelliJ e rode `./gradlew runClient` (Windows: `gradlew.bat runClient`).
4. No jogo: /give @s shadowkatana:shadow_katana
5. Para gerar o .jar: `./gradlew build` -> `build/libs/`.

Alternativa: se tiver Gradle 8.14 instalado, rode `gradle wrapper` nesta pasta.

## Controles
- **R**: sacar/guardar a katana (ela precisa estar na hotbar). Muda em Opcoes > Controles > Katana Sombria.
- **K**: menu de upgrades (dano da katana, do teleporte e do golpe sombrio; custo em niveis de XP; max nivel 50).
- **Z**: Teleporte sombrio (12 blocos, dano em area, recarga 3 s). **X**: invisibilidade 20 s, o 1º golpe depois causa dano bonus (recarga 40 s). Exigem a katana na mao.
- Ajuste da posicao/animacao: `src/client/java/com/shadowkatana/client/render/KatanaPose.java`.

## Obter a katana
Receita (formato diagonal):
```
 [Obsidiana][Netherite]
 [Obsidiana][Netherite]
 [Graveto]
```
(ou `/give @s shadowkatana:shadow_katana`)

## Gerar o .jar e instalar
1. `./gradlew build` (Windows: `gradlew.bat build`)
2. O mod fica em `build/libs/shadowkatana-0.1.0.jar` (NAO use o `-sources.jar`).
3. Instale o Fabric Loader + Fabric API para 1.21.8 e coloque o .jar na pasta `mods`.

## Checklist de teste (anote o que falhar e mande para a IA corrigir)
- [ ] O jogo abre sem crash; item aparece na aba de combate; textura preta ok
- [ ] Receita de craft funciona
- [ ] Katana aparece nas costas com ela na hotbar
- [ ] R: anima, katana vai para a mao; R de novo: guarda nas costas
- [ ] K: menu abre; subir nivel gasta XP e aumenta o dano
- [ ] Z: teleporta e causa dano; recarga aparece no HUD
- [ ] X: fica invisivel 20 s; primeiro golpe causa dano bonus; katana some das costas
- [ ] Multiplayer: outro jogador ve a katana nas costas e a animacao
