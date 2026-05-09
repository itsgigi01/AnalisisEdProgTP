# TP – Análisis Estático de Programas
**Universidad Nacional de Villa Mercedes**
Ingeniería en Sistemas de Información – 2026

Integrantes: 
  -Gil, Maria de los Angeles
  -Pedernera Cañadas, Candela Nahir
  -Galarza, Valentin

## Estructura del proyecto

```
ANEP/
├── bin/
│   └── *.class
├── lib/
│   └── javacc-7.0.13.jar
├── output/
│   ├── cdg.dot
│   ├── cdg.png
│   ├── cfg.dot
│   ├── cfg.png
    ├── ddg.dot
│   ├── ddg.png
│   ├── postdom_tree.dot
│   └── postdom_tree.png
├── test/
│   ├── test1.txt
│   ├── test2.txt
│   ├── test3.txt
│   ├── test4.txt
    ├── test5.txt da error
│   ├── test6.txt da error
│
├── Analysis.java                    ← análisis de postdominadores y CDG
├── ASTNode.java                     ← definición de nodos del AST
├── CDGBuilder.java                  ← construcción del Control Dependence Graph
├── CFGBuilder.java                  ← construcción del Control Flow Graph
├── DDGEdge.java                     ← representación de aristas del DDG
├── Definition.java                  ← representación de definiciones de variables
├── DotViz.java                      ← generación de archivos DOT y renderizado
├── Main.java                        ← programa principal
├── MiniLang.jj                      ← gramática JavaCC
├── MiniLangParser.java              ← parser generado automáticamente
├── MiniLangParserConstants.java     ← constantes del parser
├── MiniLangParserTokenManager.java  ← manejador de tokens
├── ParseException.java              ← manejo de errores sintácticos
├── PostDominatorTree.java           ← construcción del árbol de postdominadores
├── SimpleCharStream.java            ← flujo de caracteres generado
├── Token.java                       ← representación de tokens
├── TokenMgrError.java               ← errores léxicos
├── mi_programa.txt                  ← archivo de entrada de ejemplo
├── README.md
└── src/
```


## Compilar y ejecutar
# 1. Generar parser con JavaCC
java -cp ".\lib\javacc-7.0.13.jar" org.javacc.parser.Main MiniLang.jj

# 2. Compilar todo
javac -d bin (Get-ChildItem -Recurse -Filter "*.java" | Select-Object -ExpandProperty FullName)

# 3. Ejecutar
java -cp bin Main

# 4. Ejecutar con archivo propio
java -cp bin Main test/test1.txt
---


## Gramática del lenguaje

```
Programa        ::= "integer" Identificador "(" ")" "{"
                        Sentencias
                    "}"

Sentencias      ::= { Sentencia }

Sentencia       ::= Asignación
                  | If
                  | While
                  | Return

Asignación      ::= Identificador "=" Expresión ";"

If              ::= "if" "(" Expresión ")" "{"
                        Sentencias
                    "}"
                    "else" "{"
                        Sentencias
                    "}"

While           ::= "while" "(" Expresión ")" "{"
                        Sentencias
                    "}"

Return          ::= "return" Expresión ";"

Expresión       ::= Primario { "+" Primario }

Primario        ::= Identificador
                  | Entero

Identificador   ::= Letra { Letra | Dígito | "_" }

Entero          ::= Dígito { Dígito }

Letra           ::= "A".."Z" | "a".."z"
Dígito          ::= "0".."9"

```
## Salida esperada

```
Usando el programa de ejemplo 'mi_programa.text".

══════════════════════════════════════════════════════════════════
  1. PARSING  (JavaCC)
══════════════════════════════════════════════════════════════════
  Función : integer main()
  Sentencias en el cuerpo: 4

══════════════════════════════════════════════════════════════════
  2. CONTROL FLOW GRAPH (CFG)
══════════════════════════════════════════════════════════════════
  Nodos (8):
    [0] ENTRY                     ? ENTRY
    [1] x = 6                   
    [2] y = 4                   
    [3] if (y)                  
    [4] z = (x + 1)             
    [5] y = (x + z)             
    [6] return z                
    [7] EXIT                      ? EXIT

  Arcos (8):
    0  ?  1
    1  ?  2
    2  ?  3
    3  ?  4  [True]
    3  ?  5  [False]
    4  ?  6
    5  ?  6
    6  ?  7

══════════════════════════════════════════════════════════════════
  3. POSTDOMINADORES
══════════════════════════════════════════════════════════════════
  PostDom(0: "ENTRY") = { 1, 2, 3, 6, 7, 0 }
  PostDom(1: "x = 6") = { 2, 3, 6, 7, 1 }
  PostDom(2: "y = 4") = { 3, 6, 7, 2 }
  PostDom(3: "if (y)") = { 6, 7, 3 }
  PostDom(4: "z = (x + 1)") = { 6, 7, 4 }
  PostDom(5: "y = (x + z)") = { 6, 7, 5 }
  PostDom(6: "return z") = { 7, 6 }
  PostDom(7: "EXIT") = { 7 }

══════════════════════════════════════════════════════════════════
  4. ÁRBOL DE POSTDOMINADORES  (ipdom)
══════════════════════════════════════════════════════════════════
  ipdom(0: "ENTRY") = 1: "x = 6"
  ipdom(1: "x = 6") = 2: "y = 4"
  ipdom(2: "y = 4") = 3: "if (y)"
  ipdom(3: "if (y)") = 6: "return z"
  ipdom(4: "z = (x + 1)") = 6: "return z"
  ipdom(5: "y = (x + z)") = 6: "return z"
  ipdom(6: "return z") = 7: "EXIT"
  ipdom(7: "EXIT") = null  (raíz del árbol)

══════════════════════════════════════════════════════════════════
  5. CONTROL DEPENDENCE GRAPH (CDG)
══════════════════════════════════════════════════════════════════
  0:"ENTRY"  ?cd?  1:"x = 6"
  0:"ENTRY"  ?cd?  2:"y = 4"
  0:"ENTRY"  ?cd?  4:"z = (x + 1)"
  0:"ENTRY"  ?cd?  5:"y = (x + z)"
  0:"ENTRY"  ?cd?  6:"return z"
  3:"if (y)"  ?cd?  4:"z = (x + 1)"
  3:"if (y)"  ?cd?  5:"y = (x + z)"
  0:"ENTRY"  ?cd?  3:"if (y)"

══════════════════════════════════════════════════════════════════
  6. DATA DEPENDENCE GRAPH (DDG)
══════════════════════════════════════════════════════════════════
  2:"y = 4"  ?dd?  3:"if (y)" (y)
  1:"x = 6"  ?dd?  4:"z = (x + 1)" (x)
  1:"x = 6"  ?dd?  5:"y = (x + z)" (x)
  4:"z = (x + 1)"  ?dd?  6:"return z" (z)

DOT guardado: output\cfg.dot
PNG generado: output\cfg.png
DOT guardado: output\ddg.dot
PNG generado: output\ddg.png
DOT guardado: output\postdom_tree.dot
PNG generado: output\postdom_tree.png
DOT guardado: output\cdg.dot
PNG generado: output\cdg.png
══════════════════════════════════════════════════════════════════
  ANÁLISIS COMPLETADO
══════════════════════════════════════════════════════════════════
  Archivos generados en output/
    - cfg.png
    - postdom_tree.png
    - cdg.png
    - ddg.png
```


