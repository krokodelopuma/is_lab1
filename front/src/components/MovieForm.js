import React, { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { toast } from 'react-toastify';
import axios from 'axios';
import CoordinatesSelector from './CoordinatesSelector';
import PersonSelector from './PersonSelector';
import './MovieForm.css';

/**
 * Компонент формы для создания и редактирования фильмов.
 * 
 * Этот компонент предоставляет пользовательский интерфейс для:
 * - Создания новых фильмов
 * - Редактирования существующих фильмов
 * - Валидации данных на клиентской стороне
 * - Выбора существующих координат и людей или создания новых
 * 
 * Основные функции:
 * 1. Загрузка данных фильма для редактирования
 * 2. Валидация полей формы (бюджет, кассовые сборы, длительность)
 * 3. Обработка отправки формы с правильным форматированием данных
 * 4. Интеграция с компонентами выбора координат и людей
 * 
 * @author Movie Management System Team
 * @version 1.0
 */

const API_BASE_URL = 'http://localhost:8080/back-1.0-SNAPSHOT/api';

const MovieForm = () => {
  const { id } = useParams();
  const navigate = useNavigate();
  const isEdit = !!id;

  const [formData, setFormData] = useState({
    name: '',
    coordinates: { x: 0, y: 0 },
    oscarsCount: 0,
    budget: '',
    totalBoxOffice: 0,
    mpaaRating: 'G',
    director: { 
      name: '', 
      birthday: '', 
      eyeColor: '', 
      hairColor: '', 
      nationality: '',
      location: { x: 0, y: 0, z: 0 }
    },
    screenwriter: { 
      name: '', 
      birthday: '', 
      eyeColor: '', 
      hairColor: '', 
      nationality: '',
      location: { x: 0, y: 0, z: 0 }
    },
    operator: { 
      name: '', 
      birthday: '', 
      eyeColor: '', 
      hairColor: '', 
      nationality: '',
      location: { x: 0, y: 0, z: 0 }
    },
    length: 0,
    goldenPalmCount: '',
    genre: 'DRAMA'
  });

  const [loading, setLoading] = useState(false);
  const [validationErrors, setValidationErrors] = useState({});

  useEffect(() => {
    if (isEdit) {
      fetchMovie();
    }
  }, [id]);

  const fetchMovie = async () => {
    try {
      setLoading(true);
      const response = await axios.get(`${API_BASE_URL}/movies/${id}`);
      const movie = response.data;
      
      setFormData({
        name: movie.name || '',
        coordinates: movie.coordinates || { x: 0, y: 0 },
        oscarsCount: movie.oscarsCount || 0,
        budget: movie.budget || '',
        totalBoxOffice: movie.totalBoxOffice || 0,
        mpaaRating: movie.mpaaRating || 'G',
        director: movie.director || { 
          name: '', 
          birthday: '', 
          eyeColor: '', 
          hairColor: '', 
          nationality: '',
          location: { x: 0, y: 0, z: 0 }
        },
        screenwriter: movie.screenwriter || { 
          name: '', 
          birthday: '', 
          eyeColor: '', 
          hairColor: '', 
          nationality: '',
          location: { x: 0, y: 0, z: 0 }
        },
        operator: movie.operator || { 
          name: '', 
          birthday: '', 
          eyeColor: '', 
          hairColor: '', 
          nationality: '',
          location: { x: 0, y: 0, z: 0 }
        },
        length: movie.length || 0,
        goldenPalmCount: movie.goldenPalmCount || '',
        genre: movie.genre || 'DRAMA'
      });
    } catch (error) {
      toast.error('Ошибка загрузки фильма: ' + error.message);
      navigate('/');
    } finally {
      setLoading(false);
    }
  };

  const validateNumericField = (name, value) => {
    if (value === '' || value === null) return true;
    
    const numValue = Number(value);
    if (isNaN(numValue)) return false;
    
    switch (name) {
      case 'oscarsCount':
        return numValue >= 0;
      case 'budget':
        return numValue > 0;
      case 'totalBoxOffice':
        return numValue > 0;
      case 'length':
        return numValue > 0;
      case 'goldenPalmCount':
        return numValue >= 0;
      default:
        return true;
    }
  };

  const handleInputChange = (e) => {
    const { name, value } = e.target;
    
    if (name.includes('.')) {
      const parts = name.split('.');
      if (parts.length === 2) {
        const [parent, child] = parts;
        setFormData(prev => ({
          ...prev,
          [parent]: {
            ...prev[parent],
            [child]: value
          }
        }));
      } else if (parts.length === 3) {
        const [parent, child, grandchild] = parts;
        setFormData(prev => ({
          ...prev,
          [parent]: {
            ...prev[parent],
            [child]: {
              ...prev[parent][child],
              [grandchild]: value === '' ? '' : (isNaN(value) ? value : Number(value))
            }
          }
        }));
      }
    } else {
      // For numeric fields, convert to number if it's a valid number, otherwise keep as string
      const numericFields = ['oscarsCount', 'totalBoxOffice', 'length', 'budget', 'goldenPalmCount'];
      if (numericFields.includes(name)) {
        // Проверяем валидность числового поля
        const isValid = validateNumericField(name, value);
        
        // Обновляем состояние ошибок
        setValidationErrors(prev => ({
          ...prev,
          [name]: isValid ? null : `${name === 'budget' || name === 'totalBoxOffice' || name === 'length' ? 'Должно быть положительным числом' : 'Должно быть неотрицательным числом'}`
        }));
        
        setFormData(prev => ({
          ...prev,
          [name]: value === '' ? '' : (isNaN(value) ? value : Number(value))
        }));
      } else {
        setFormData(prev => ({
          ...prev,
          [name]: value
        }));
      }
    }
  };

  const validateForm = () => {
    const errors = [];

    // Проверка названия
    if (!formData.name || formData.name.trim() === '') {
      errors.push('Название фильма обязательно для заполнения');
    }

    // Проверка количества Оскаров
    if (formData.oscarsCount < 0) {
      errors.push('Количество Оскаров не может быть отрицательным');
    }

    // Проверка бюджета (если указан)
    if (formData.budget !== '' && formData.budget !== null) {
      if (formData.budget <= 0) {
        errors.push('Бюджет должен быть положительным числом');
      }
    }

    // Проверка кассовых сборов
    if (formData.totalBoxOffice <= 0) {
      errors.push('Кассовые сборы должны быть положительным числом');
    }

    // Проверка длительности
    if (formData.length <= 0) {
      errors.push('Длительность фильма должна быть положительным числом');
    }

    // Проверка золотых пальм (если указаны)
    if (formData.goldenPalmCount !== '' && formData.goldenPalmCount !== null) {
      if (formData.goldenPalmCount < 0) {
        errors.push('Количество золотых пальм не может быть отрицательным');
      }
    }

    // Проверка режиссера
    if (!formData.director.name || formData.director.name.trim() === '') {
      errors.push('Имя режиссера обязательно для заполнения');
    }
    if (!formData.director.birthday) {
      errors.push('Дата рождения режиссера обязательна для заполнения');
    }

    // Проверка оператора
    if (!formData.operator.name || formData.operator.name.trim() === '') {
      errors.push('Имя оператора обязательно для заполнения');
    }
    if (!formData.operator.birthday) {
      errors.push('Дата рождения оператора обязательна для заполнения');
    }

    // Проверка сценариста (если указан)
    if (formData.screenwriter.name && formData.screenwriter.name.trim() !== '') {
      if (!formData.screenwriter.birthday) {
        errors.push('Если указан сценарист, дата его рождения обязательна');
      }
    }

    return errors;
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    
    // Валидация формы
    const validationErrors = validateForm();
    if (validationErrors.length > 0) {
      validationErrors.forEach(error => toast.error(error));
      return;
    }
    
    try {
      setLoading(true);
      
      // Prepare data for submission - convert empty strings to null for optional fields
      const submitData = {
        ...formData,
        budget: formData.budget === '' ? null : formData.budget,
        goldenPalmCount: formData.goldenPalmCount === '' ? null : formData.goldenPalmCount,
        director: {
          ...formData.director,
          eyeColor: formData.director.eyeColor === '' ? null : formData.director.eyeColor,
          hairColor: formData.director.hairColor === '' ? null : formData.director.hairColor,
          nationality: formData.director.nationality === '' ? null : formData.director.nationality,
          location: formData.director.location.x === 0 && formData.director.location.y === 0 && formData.director.location.z === 0 ? null : formData.director.location
        },
        screenwriter: {
          ...formData.screenwriter,
          name: formData.screenwriter.name === '' ? null : formData.screenwriter.name,
          birthday: formData.screenwriter.birthday === '' ? null : formData.screenwriter.birthday,
          eyeColor: formData.screenwriter.eyeColor === '' ? null : formData.screenwriter.eyeColor,
          hairColor: formData.screenwriter.hairColor === '' ? null : formData.screenwriter.hairColor,
          nationality: formData.screenwriter.nationality === '' ? null : formData.screenwriter.nationality,
          location: formData.screenwriter.location.x === 0 && formData.screenwriter.location.y === 0 && formData.screenwriter.location.z === 0 ? null : formData.screenwriter.location
        },
        operator: {
          ...formData.operator,
          eyeColor: formData.operator.eyeColor === '' ? null : formData.operator.eyeColor,
          hairColor: formData.operator.hairColor === '' ? null : formData.operator.hairColor,
          nationality: formData.operator.nationality === '' ? null : formData.operator.nationality,
          location: formData.operator.location.x === 0 && formData.operator.location.y === 0 && formData.operator.location.z === 0 ? null : formData.operator.location
        }
      };
      
      console.log('Отправляемые данные:', JSON.stringify(submitData, null, 2));
      
      if (isEdit) {
        await axios.put(`${API_BASE_URL}/movies/${id}`, submitData);
        toast.success('Фильм успешно обновлен');
      } else {
        await axios.post(`${API_BASE_URL}/movies`, submitData);
        toast.success('Фильм успешно создан');
      }
      
      navigate('/');
    } catch (error) {
      console.error('Ошибка сохранения фильма:', error);
      if (error.response) {
        console.error('Ответ сервера:', error.response.data);
        console.error('Статус:', error.response.status);
        toast.error('Ошибка сохранения фильма: ' + (error.response.data?.error || error.message));
      } else {
        toast.error('Ошибка сохранения фильма: ' + error.message);
      }
    } finally {
      setLoading(false);
    }
  };

  const handleCancel = () => {
    navigate('/');
  };

  if (loading && isEdit) {
    return <div className="loading">Загрузка фильма...</div>;
  }

  return (
    <div className="movie-form">
      <div className="form-header">
        <h1>{isEdit ? 'Редактировать фильм' : 'Создать новый фильм'}</h1>
        <button onClick={handleCancel} className="btn btn-secondary">
          Отмена
        </button>
      </div>

      <form onSubmit={handleSubmit} className="form">
        <div className="form-section">
          <h3>Основная информация</h3>
          <div className="form-row">
            <div className="form-group">
              <label htmlFor="name">Название *</label>
              <input
                type="text"
                id="name"
                name="name"
                value={formData.name}
                onChange={handleInputChange}
                required
                className="form-input"
              />
            </div>
            <div className="form-group">
              <label htmlFor="genre">Жанр *</label>
              <select
                id="genre"
                name="genre"
                value={formData.genre}
                onChange={handleInputChange}
                required
                className="form-input"
              >
                <option value="DRAMA">DRAMA</option>
                <option value="ADVENTURE">ADVENTURE</option>
                <option value="FANTASY">FANTASY</option>
              </select>
            </div>
          </div>

          <div className="form-row">
            <div className="form-group">
              <label htmlFor="oscarsCount">Количество Оскаров *</label>
              <input
                type="number"
                id="oscarsCount"
                name="oscarsCount"
                value={formData.oscarsCount}
                onChange={handleInputChange}
                min="0"
                required
                className={`form-input ${validationErrors.oscarsCount ? 'error' : ''}`}
              />
              {validationErrors.oscarsCount && (
                <span className="error-message">{validationErrors.oscarsCount}</span>
              )}
            </div>
            <div className="form-group">
              <label htmlFor="budget">Бюджет</label>
              <input
                type="number"
                id="budget"
                name="budget"
                value={formData.budget}
                onChange={handleInputChange}
                min="0"
                step="0.01"
                className={`form-input ${validationErrors.budget ? 'error' : ''}`}
              />
              {validationErrors.budget && (
                <span className="error-message">{validationErrors.budget}</span>
              )}
            </div>
          </div>

          <div className="form-row">
            <div className="form-group">
              <label htmlFor="totalBoxOffice">Кассовые сборы *</label>
              <input
                type="number"
                id="totalBoxOffice"
                name="totalBoxOffice"
                value={formData.totalBoxOffice}
                onChange={handleInputChange}
                min="0"
                required
                className={`form-input ${validationErrors.totalBoxOffice ? 'error' : ''}`}
              />
              {validationErrors.totalBoxOffice && (
                <span className="error-message">{validationErrors.totalBoxOffice}</span>
              )}
            </div>
            <div className="form-group">
              <label htmlFor="length">Длительность (минуты) *</label>
              <input
                type="number"
                id="length"
                name="length"
                value={formData.length}
                onChange={handleInputChange}
                min="0"
                required
                className={`form-input ${validationErrors.length ? 'error' : ''}`}
              />
              {validationErrors.length && (
                <span className="error-message">{validationErrors.length}</span>
              )}
            </div>
          </div>

          <div className="form-row">
            <div className="form-group">
              <label htmlFor="goldenPalmCount">Количество Золотых пальм</label>
              <input
                type="number"
                id="goldenPalmCount"
                name="goldenPalmCount"
                value={formData.goldenPalmCount}
                onChange={handleInputChange}
                min="0"
                className={`form-input ${validationErrors.goldenPalmCount ? 'error' : ''}`}
              />
              {validationErrors.goldenPalmCount && (
                <span className="error-message">{validationErrors.goldenPalmCount}</span>
              )}
            </div>
            <div className="form-group">
              <label htmlFor="mpaaRating">Рейтинг MPAA *</label>
              <select
                id="mpaaRating"
                name="mpaaRating"
                value={formData.mpaaRating}
                onChange={handleInputChange}
                required
                className="form-input"
              >
                <option value="G">G</option>
                <option value="PG_13">PG_13</option>
                <option value="NC_17">NC_17</option>
              </select>
            </div>
          </div>
        </div>

        <div className="form-section">
          <CoordinatesSelector
            value={formData.coordinates}
            onChange={(coordinates) => setFormData(prev => ({ ...prev, coordinates }))}
            label="Координаты фильма"
          />
        </div>

        <div className="form-section">
          <PersonSelector
            value={formData.director}
            onChange={(director) => setFormData(prev => ({ ...prev, director }))}
            label="Режиссер"
            required={true}
          />
        </div>

        <div className="form-section">
          <PersonSelector
            value={formData.screenwriter}
            onChange={(screenwriter) => setFormData(prev => ({ ...prev, screenwriter }))}
            label="Сценарист"
            required={false}
          />
        </div>

        <div className="form-section">
          <PersonSelector
            value={formData.operator}
            onChange={(operator) => setFormData(prev => ({ ...prev, operator }))}
            label="Оператор"
            required={true}
          />
        </div>

        <div className="form-actions">
          <button type="button" onClick={handleCancel} className="btn btn-secondary">
            Отмена
          </button>
          <button type="submit" disabled={loading} className="btn btn-primary">
            {loading ? 'Сохранение...' : (isEdit ? 'Обновить фильм' : 'Создать фильм')}
          </button>
        </div>
      </form>
    </div>
  );
};

export default MovieForm;
