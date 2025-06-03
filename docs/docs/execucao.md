# Como executar o projeto
Para rodar o projeto, é necessário apenas que você possua o Maven instalado. Para checar isso, digite em seu terminal:
```bash
mvn --version
```
Caso não esteja instalado, instale-o:

```bash
# Windows
choco install maven # ou
scoop install maven

# Linux
sudo apt install maven # ou
sudo dnf install maven

# macOS
brew install maven
```

Após isso, basta rodar o script auxiliar:
```bash
# Linux
chmod +x run.sh
./run.sh

# Windows
.\run.bat

# macOS
chmod +x run.sh
./run.sh # ou
sh run.sh
```

Então, o Maven cuidará de todo o processo de build e o sistema será iniciado.

# Como acessar o dashboard 
Para utilizar o dashboard, basta rodar o `run.sh` ou `run.bat` e visitar o endereço [localhost:8080](http://localhost:8080/).
