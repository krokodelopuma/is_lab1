import React, { useState, useEffect } from 'react';
import axios from 'axios';
import './SelectorStyles.css';

const API_BASE_URL = 'http://localhost:8080/back-1.0-SNAPSHOT/api';

const CoordinatesSelector = ({ value, onChange, label }) => {
  const [existingCoordinates, setExistingCoordinates] = useState([]);
  const [loading, setLoading] = useState(false);
  const [useExisting, setUseExisting] = useState(false);
  const [selectedId, setSelectedId] = useState('');

  useEffect(() => {
    fetchExistingCoordinates();
  }, []);

  const fetchExistingCoordinates = async () => {
    try {
      setLoading(true);
      const response = await axios.get(`${API_BASE_URL}/coordinates`);
      setExistingCoordinates(response.data);
    } catch (error) {
      console.error('Error fetching coordinates:', error);
    } finally {
      setLoading(false);
    }
  };

  const handleModeChange = (mode) => {
    setUseExisting(mode === 'existing');
    if (mode === 'new') {
      onChange({ x: 0, y: 0 });
      setSelectedId('');
    }
  };

  const handleExistingChange = (e) => {
    const id = e.target.value;
    setSelectedId(id);
    if (id) {
      const selected = existingCoordinates.find(c => c.id === parseInt(id));
      if (selected) {
        onChange(selected);
      }
    }
  };

  const handleNewChange = (field, val) => {
    onChange({
      ...value,
      [field]: parseFloat(val) || 0
    });
  };

  return (
    <div className="coordinates-selector">
      <label className="form-label">{label}</label>
      
      <div className="mode-selector">
        <label>
          <input
            type="radio"
            name={`${label}-mode`}
            checked={!useExisting}
            onChange={() => handleModeChange('new')}
          />
          Создать новые координаты
        </label>
        <label>
          <input
            type="radio"
            name={`${label}-mode`}
            checked={useExisting}
            onChange={() => handleModeChange('existing')}
          />
          Выбрать существующие
        </label>
      </div>

      {useExisting ? (
        <div className="existing-selector">
          <select
            value={selectedId}
            onChange={handleExistingChange}
            className="form-select"
            disabled={loading}
          >
            <option value="">Выберите координаты...</option>
            {existingCoordinates.map(coord => (
              <option key={coord.id} value={coord.id}>
                X: {coord.x}, Y: {coord.y}
              </option>
            ))}
          </select>
          {loading && <div className="loading">Загрузка...</div>}
        </div>
      ) : (
        <div className="new-coordinates">
          <div className="form-group">
            <label>X:</label>
            <input
              type="number"
              value={value.x}
              onChange={(e) => handleNewChange('x', e.target.value)}
              className="form-control"
            />
          </div>
          <div className="form-group">
            <label>Y:</label>
            <input
              type="number"
              step="0.1"
              value={value.y}
              onChange={(e) => handleNewChange('y', e.target.value)}
              className="form-control"
            />
          </div>
        </div>
      )}
    </div>
  );
};

export default CoordinatesSelector;
