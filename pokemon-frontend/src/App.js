import React, { useState, useEffect, useRef } from "react";
import axios from "axios";
import InfiniteScroll from "react-infinite-scroll-component";
import PokemonCard from "./components/PokemonCard";
import './App.css';

function App() {
  const [pokemonList, setPokemonList] = useState([]);
  const [hasMore, setHasMore] = useState(true);
  const [initialLoad, setInitialLoad] = useState(true);

  const pageSize = 20;
  const pageRef = useRef(0);       // track current page
  const loadingRef = useRef(false); // track loading state

  const fetchPokemon = async () => {
    if (loadingRef.current || !hasMore) return;

    loadingRef.current = true;
    try {
      console.log(`Fetching page: ${pageRef.current}`);
      const res = await axios.get(`http://localhost:8080/api/pokemons?page=${pageRef.current}&size=${pageSize}`);
      const newPokemons = res.data;

      if (!Array.isArray(newPokemons) || newPokemons.length === 0) {
        setHasMore(false);
        return;
      }

      setPokemonList(prev => {
        // Remove duplicates just in case
        const existingIds = new Set(prev.map(p => p.id));
        const filteredNew = newPokemons.filter(p => !existingIds.has(p.id));
        return [...prev, ...filteredNew];
      });

      pageRef.current += 1; // increment page
      if (newPokemons.length < pageSize) setHasMore(false);

    } catch (error) {
      console.error("Error fetching Pokémon:", error);
      setHasMore(false);
      if (error.code === 'ERR_NETWORK') {
        alert('Backend connection failed! Make sure your Spring Boot server is running on localhost:8080');
      }
    } finally {
      loadingRef.current = false;
      setInitialLoad(false);
    }
  };

  const refreshData = () => {
    setPokemonList([]);
    pageRef.current = 0;
    setHasMore(true);
    setInitialLoad(true);
  };

  useEffect(() => {
    fetchPokemon(); // fetch initial data
  }, []);

  return (
    <div className="app">
      <header className="app-header">
        <h1>🔥 Bolttech Pokémon Catchers</h1>
        <p>Catch the rarest and most powerful Pokémon!</p>
        <button onClick={refreshData} className="refresh-btn">
          🔄 Refresh
        </button>
      </header>

      {initialLoad && (
        <div className="initial-loader">
          <h3>🔍 Searching for Pokémon...</h3>
          <p>Loading your team from the Pokédex...</p>
        </div>
      )}

      {pokemonList.length === 0 && !initialLoad && (
        <div className="error-message">
          <h3>❌ No Pokémon Found!</h3>
          <p>Make sure your backend is running on <strong>localhost:8080</strong></p>
          <p>Try: <code>mvn spring-boot:run</code></p>
          <button onClick={refreshData}>🔄 Try Again</button>
        </div>
      )}

      {pokemonList.length > 0 && (
        <InfiniteScroll
          dataLength={pokemonList.length}
          next={fetchPokemon}
          hasMore={hasMore}
          loader={
            <div className="scroll-loader">
              <h4>🔄 Catching more Pokémon...</h4>
            </div>
          }
          endMessage={
            <div className="end-message">
              <p>🎉 <strong>Congratulations!</strong> You've caught them all!</p>
              <p>Total Pokémon caught: {pokemonList.length}</p>
            </div>
          }
        >
          <div className="pokemon-grid">
            {pokemonList.map(pokemon => (
              <PokemonCard key={pokemon.id} pokemon={pokemon} />
            ))}
          </div>
        </InfiniteScroll>
      )}

      <div className="debug-info">
        <div>Total: {pokemonList.length}</div>
        <div>HasMore: {hasMore ? 'Yes' : 'No'}</div>
        <div>Loading: {loadingRef.current ? 'Yes' : 'No'}</div>
        <div>Current Page: {pageRef.current}</div>
      </div>
    </div>
  );
}

export default App;
