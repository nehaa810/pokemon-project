import React, { useState } from 'react';
import './PokemonCard.css';

// Pokemon Detail Modal Component
const PokemonModal = ({ pokemon, isOpen, onClose }) => {
  if (!isOpen || !pokemon) return null;

  const getTypeColor = (type) => {
    const colors = {
      normal: '#A8A878', fire: '#F08030', water: '#6890F0',
      electric: '#F8D030', grass: '#78C850', ice: '#98D8D8',
      fighting: '#C03028', poison: '#A040A0', ground: '#E0C068',
      flying: '#A890F0', psychic: '#F85888', bug: '#A8B820',
      rock: '#B8A038', ghost: '#705898', dragon: '#7038F8',
      dark: '#705848', steel: '#B8B8D0', fairy: '#EE99AC'
    };
    return colors[type?.toLowerCase()] || '#68A090';
  };

  return (
    <div className="modal-overlay" onClick={onClose}>
      <div className="modal-content" onClick={(e) => e.stopPropagation()}>
        <div className="modal-header">
          <h2 className="modal-title">{pokemon.name}</h2>
          <button className="modal-close" onClick={onClose}>✕</button>
        </div>

        <div className="modal-body">
          <div className="pokemon-images">
            <div className="image-container">
              <p className="image-label">Front</p>
              <img
                src={pokemon.frontImage || pokemon.sprites?.front_default}
                alt={`${pokemon.name} front`}
                className="pokemon-image"
                onError={(e) => {
                  e.target.src = 'https://via.placeholder.com/96x96?text=Front';
                }}
              />
            </div>
            <div className="image-container">
              <p className="image-label">Back</p>
              <img
                src={pokemon.backImage || pokemon.sprites?.back_default}
                alt={`${pokemon.name} back`}
                className="pokemon-image"
                onError={(e) => {
                  e.target.src = 'https://via.placeholder.com/96x96?text=Back';
                }}
              />
            </div>
          </div>

          <div className="pokemon-details">
            <div className="detail-section">
              <div className="detail-header">
                <span className="detail-icon">⚡</span>
                <span className="detail-label">Types</span>
              </div>
              <div className="type-badges">
                {(pokemon.types || []).map((type, index) => (
                  <span
                    key={index}
                    className="type-badge"
                    style={{ backgroundColor: getTypeColor(typeof type === 'string' ? type : type.type?.name) }}
                  >
                    {typeof type === 'string' ? type : type.type?.name}
                  </span>
                ))}
              </div>
            </div>

            {pokemon.region && (
              <div className="detail-section">
                <div className="detail-header">
                  <span className="detail-icon">📍</span>
                  <span className="detail-label">Region</span>
                </div>
                <span className="detail-value">{pokemon.region}</span>
              </div>
            )}

            {pokemon.weaknesses && pokemon.weaknesses.length > 0 && (
              <div className="detail-section">
                <div className="detail-header">
                  <span className="detail-icon">🛡️</span>
                  <span className="detail-label">Weaknesses</span>
                </div>
                <div className="weakness-badges">
                  {pokemon.weaknesses.map((weakness, index) => (
                    <span key={index} className="weakness-badge">
                      {weakness}
                    </span>
                  ))}
                </div>
              </div>
            )}
          </div>
        </div>
      </div>
    </div>
  );
};

// Main Pokemon Card Component
const PokemonCard = ({ pokemon }) => {
  const [showModal, setShowModal] = useState(false);

  const getTypeColor = (type) => {
    const colors = {
      normal: '#A8A878', fire: '#F08030', water: '#6890F0',
      electric: '#F8D030', grass: '#78C850', ice: '#98D8D8',
      fighting: '#C03028', poison: '#A040A0', ground: '#E0C068',
      flying: '#A890F0', psychic: '#F85888', bug: '#A8B820',
      rock: '#B8A038', ghost: '#705898', dragon: '#7038F8',
      dark: '#705848', steel: '#B8B8D0', fairy: '#EE99AC'
    };
    return colors[type?.toLowerCase()] || '#68A090';
  };

  const handleCardClick = () => {
    setShowModal(true);
  };

  const closeModal = () => {
    setShowModal(false);
  };

  return (
    <>
      <div className="pokemon-card" onClick={handleCardClick}>
        <div className="pokemon-card-image">
          <img
            src={pokemon.frontImage || pokemon.sprites?.front_default}
            alt={pokemon.name}
            onError={(e) => {
              e.target.src = 'https://via.placeholder.com/128x128?text=Pokemon';
            }}
          />
        </div>
        
        <div className="pokemon-card-content">
          <h3 className="pokemon-name">{pokemon.name}</h3>
          
          <div className="pokemon-types">
            {(pokemon.types || []).map((type, index) => (
              <span
                key={index}
                className="pokemon-type"
                style={{ backgroundColor: getTypeColor(typeof type === 'string' ? type : type.type?.name) }}
              >
                {typeof type === 'string' ? type : type.type?.name}
              </span>
            ))}
          </div>
        </div>
      </div>

      <PokemonModal
        pokemon={pokemon}
        isOpen={showModal}
        onClose={closeModal}
      />
    </>
  );
};

export default PokemonCard;