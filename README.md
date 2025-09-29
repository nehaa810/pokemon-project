\# Pokémon Catchers MVP



\## Backend Setup (IntelliJ)



1\. Open `pokemon-backend` folder in \*\*IntelliJ\*\*.

2\. Make sure \*\*Redis\*\* is running on default localhost:6379



   \*\*Steps to run Redis:\*\*

   - \*\*Windows:\*\*

     1. Download Redis zip and extract

     2. Open command prompt in Redis folder → `redis-server.exe`

     3. Test Redis: `redis-cli.exe` → type `ping` → should return `PONG`



   - \*\*Mac/Linux:\*\* Use standard brew/apt commands  
     \*\*Mac:\*\*

     ```bash

     brew install redis

     brew services start redis

     ```

   - \*\*Linux:\*\*

     ```bash

     sudo apt update

     sudo apt install redis-server

     sudo systemctl start redis

     ```



3\. Run `PokemonBackendApplication` in IntelliJ.

4\. Backend runs at: `http://localhost:8080`

5\. API endpoint to fetch  Pokémons: `GET /api/pokemons?page=0\&size=10
   API endpoint to fetch by id : GET /api/pokemons/{id}

6\. Verify Redis is connected via logs or cache check.



---



\## Frontend Setup (VS Code)



1\. Open `pokemon-frontend` folder in VS Code.

2\. Run:

   ```bash

   npm install

   npm start
3. Frontend runs at: `http://localhost:4007`

   - To change port: update `package.json` start script

4\. Make sure frontend fetch URL points to backend port (default 8080)



---



\## Notes

\- Redis default config is sufficient (localhost:6379).

\- After starting backend and frontend, frontend should load Pokémon data properly.

