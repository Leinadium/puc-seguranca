# Roteiro do trabalho 2: MySignature

O objetivo do Trabalho 2(T2) é a dupla de alunos exercitar o aprendizado de aula prática de JCA e implementar
a classe MySignature para gerar e verificar a assinatura digital padrão RSA de strings. A classe pode usar os recursos
dos provedores criptográficos da JCA, mas o processo de geração e verificação da assinatura digital 
**não pode utilizar a classe Signature**.

**Os arquivos-fontes das classes implementadas (.java)** devem ter um comentário no início identificado os 
membros do grupo (nome e matrícula) e devem ser submetidos no sistema.


## Enunciado

Implementar a classe ***MySignature*** para gerar e verificar a assinatura digital padrão RSA de strings.
A classe pode usar os recursos dos provedores criptográficos da JCA, mas o processo de geração e verificação
da assinatura digital **não pode utilizar a classe *Signature***.

A classe *MySignature* deve implementar obrigatoriamente os métodos *getInstance*, *initSign*, *update*, *sign*,
*initVerify*, e *verify* com funcionalidades equivalentes aos respectivios métodos da classe *Signature* da JCA.

A classe *MySignature* **não pode herdar** e **nem instanciar um objeto** da classe *Signature*. Os métodos
obrigatórios devem ser implementados pelo programador. Outros métodos auxiliares podem ser desenvolvidos.
Os padrões de assinatura suportados devem ser "MD5withRSA", "SHA1withRSA", "SHA256withRSA", "SHA512withRSA"
(a string do padrão de assinatura é fornecida como argumento do método *getInstance*)

O programador também deve implementar a classe *MySignatureTest* para testar a classe MySignature. Essa classe
deve executar as seguintes funções:

* Receber o padrão de assinatura e a string que deve ser assinada, nesta ordem, como argumentos na linha de comando;
* Gerar o par de chaves assimétricas para gerar e verificar a assinatura digital da string recebida na linha de comando;
* Instanciar e usar os métodos da classe MySignature para gerar e verificar a assinatura digital da string no padrão soliticado;
* Imprimir, na saída padrão, todos os passos executados para a geração do par de chaves assimétricas e para a geração e a verificação da assinatura digital;
* Imprimir, na saída padrão, o resumo de mensagem (digest) e a assinatura digital no formato hexadecimal.

Ambos os **arquivos-fontes das classes** (*MySignature.java* e *MySignatureTest.java*) devem ter um comentário no início
identificando os membros do grupo (nome e matrícula) e devem ser submetidos no sistema.