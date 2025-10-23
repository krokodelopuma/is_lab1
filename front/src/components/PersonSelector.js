import React, { useState, useEffect } from 'react';
import axios from 'axios';
import './SelectorStyles.css';

const API_BASE_URL = 'http://localhost:8080/back-1.0-SNAPSHOT/api';

const PersonSelector = ({ value, onChange, label, required = false }) => {
  const [existingPersons, setExistingPersons] = useState([]);
  const [loading, setLoading] = useState(false);
  const [useExisting, setUseExisting] = useState(false);
  const [selectedId, setSelectedId] = useState('');

  useEffect(() => {
    fetchExistingPersons();
  }, []);

  const fetchExistingPersons = async () => {
    try {
      setLoading(true);
      const response = await axios.get(`${API_BASE_URL}/persons`);
      setExistingPersons(response.data);
    } catch (error) {
      console.error('Error fetching persons:', error);
    } finally {
      setLoading(false);
    }
  };

  const handleModeChange = (mode) => {
    setUseExisting(mode === 'existing');
    if (mode === 'new') {
      onChange({
        name: '',
        birthday: '',
        eyeColor: '',
        hairColor: '',
        nationality: '',
        location: { x: 0, y: 0, z: 0 }
      });
      setSelectedId('');
    }
  };

  const handleExistingChange = (e) => {
    const id = e.target.value;
    setSelectedId(id);
    if (id) {
      const selected = existingPersons.find(p => p.id === parseInt(id));
      if (selected) {
        onChange(selected);
      }
    }
  };

  const handleNewChange = (field, val) => {
    if (field.includes('.')) {
      const parts = field.split('.');
      if (parts.length === 2) {
        const [parent, child] = parts;
        onChange({
          ...value,
          [parent]: {
            ...value[parent],
            [child]: val === '' ? '' : (isNaN(val) ? val : Number(val))
          }
        });
      }
    } else {
      onChange({
        ...value,
        [field]: val
      });
    }
  };

  return (
    <div className="person-selector">
      <label className="form-label">
        {label}
        {required && <span className="required">*</span>}
      </label>
      
      <div className="mode-selector">
        <label>
          <input
            type="radio"
            name={`${label}-mode`}
            checked={!useExisting}
            onChange={() => handleModeChange('new')}
          />
          Создать нового человека
        </label>
        <label>
          <input
            type="radio"
            name={`${label}-mode`}
            checked={useExisting}
            onChange={() => handleModeChange('existing')}
          />
          Выбрать существующего
        </label>
      </div>

      {useExisting ? (
        <div className="existing-selector">
          <select
            value={selectedId}
            onChange={handleExistingChange}
            className="form-select"
            disabled={loading}
            required={required}
          >
            <option value="">Выберите человека...</option>
            {existingPersons.map(person => (
              <option key={person.id} value={person.id}>
                {person.name} ({person.birthday})
              </option>
            ))}
          </select>
          {loading && <div className="loading">Загрузка...</div>}
        </div>
      ) : (
        <div className="new-person">
          <div className="form-group">
            <label>Имя:</label>
            <input
              type="text"
              value={value.name}
              onChange={(e) => handleNewChange('name', e.target.value)}
              className="form-control"
              required={required}
            />
          </div>
          
          <div className="form-group">
            <label>Дата рождения:</label>
            <input
              type="date"
              value={value.birthday}
              onChange={(e) => handleNewChange('birthday', e.target.value)}
              className="form-control"
              required={required}
            />
          </div>
          
          <div className="form-group">
            <label>Цвет глаз:</label>
            <select
              value={value.eyeColor}
              onChange={(e) => handleNewChange('eyeColor', e.target.value)}
              className="form-control"
            >
              <option value="">Выберите цвет...</option>
              <option value="RED">Красный</option>
              <option value="BLUE">Синий</option>
              <option value="YELLOW">Желтый</option>
              <option value="ORANGE">Оранжевый</option>
              <option value="WHITE">Белый</option>
            </select>
          </div>
          
          <div className="form-group">
            <label>Цвет волос:</label>
            <select
              value={value.hairColor}
              onChange={(e) => handleNewChange('hairColor', e.target.value)}
              className="form-control"
            >
              <option value="">Выберите цвет...</option>
              <option value="RED">Красный</option>
              <option value="BLUE">Синий</option>
              <option value="YELLOW">Желтый</option>
              <option value="ORANGE">Оранжевый</option>
              <option value="WHITE">Белый</option>
            </select>
          </div>
          
          <div className="form-group">
            <label>Национальность:</label>
            <select
              value={value.nationality}
              onChange={(e) => handleNewChange('nationality', e.target.value)}
              className="form-control"
            >
              <option value="">Выберите национальность...</option>
              <option value="UNITED_KINGDOM">Великобритания</option>
              <option value="FRANCE">Франция</option>
              <option value="SPAIN">Испания</option>
              <option value="THAILAND">Таиланд</option>
            </select>
          </div>
          
          <div className="location-fields">
            <h4>Местоположение:</h4>
            <div className="form-group">
              <label>X:</label>
              <input
                type="number"
                value={value.location.x}
                onChange={(e) => handleNewChange('location.x', e.target.value)}
                className="form-control"
              />
            </div>
            <div className="form-group">
              <label>Y:</label>
              <input
                type="number"
                value={value.location.y}
                onChange={(e) => handleNewChange('location.y', e.target.value)}
                className="form-control"
              />
            </div>
            <div className="form-group">
              <label>Z:</label>
              <input
                type="number"
                step="0.1"
                value={value.location.z}
                onChange={(e) => handleNewChange('location.z', e.target.value)}
                className="form-control"
              />
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

export default PersonSelector;
