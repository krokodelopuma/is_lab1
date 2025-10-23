import React, { useState, useEffect } from 'react';
import { toast } from 'react-toastify';
import axios from 'axios';
import './SpecialOperations.css';

const API_BASE_URL = 'http://localhost:8080/back-1.0-SNAPSHOT/api';

const SpecialOperations = () => {
  const [loading, setLoading] = useState(false);
  const [results, setResults] = useState(null);
  const [availableGenres, setAvailableGenres] = useState([]);

  useEffect(() => {
    fetchAvailableGenres();
  }, []);

  const fetchAvailableGenres = async () => {
    try {
      const response = await axios.get(`${API_BASE_URL}/movies/genres`);
      setAvailableGenres(response.data);
    } catch (error) {
      console.error('Ошибка загрузки жанров:', error);
    }
  };

  const handleDeleteByGoldenPalm = async (e) => {
    e.preventDefault();
    const goldenPalmCount = e.target.goldenPalmCount.value;
    
    if (!goldenPalmCount) {
      toast.error('Пожалуйста, введите количество Золотых пальм');
      return;
    }

    if (window.confirm(`Вы уверены, что хотите удалить фильмы с ${goldenPalmCount} Золотыми пальмами?`)) {
      try {
        setLoading(true);
        await axios.delete(`${API_BASE_URL}/movies/by-golden-palm/${goldenPalmCount}`);
        toast.success('Фильмы успешно удалены');
        setResults({ operation: 'delete', goldenPalmCount });
      } catch (error) {
        toast.error('Ошибка удаления фильмов: ' + error.message);
      } finally {
        setLoading(false);
      }
    }
  };

  const handleGetSum = async () => {
    try {
      setLoading(true);
      const response = await axios.get(`${API_BASE_URL}/movies/sum-golden-palm`);
      setResults({ operation: 'sum', sum: response.data.sum });
      toast.success('Сумма успешно вычислена');
    } catch (error) {
      toast.error('Ошибка вычисления суммы: ' + error.message);
    } finally {
      setLoading(false);
    }
  };

  const handleGetMoviesByGoldenPalm = async (e) => {
    e.preventDefault();
    const goldenPalmCount = e.target.goldenPalmCount.value;
    
    if (!goldenPalmCount) {
      toast.error('Пожалуйста, введите количество Золотых пальм');
      return;
    }

    try {
      setLoading(true);
      const response = await axios.get(`${API_BASE_URL}/movies/by-golden-palm-greater/${goldenPalmCount}`);
      setResults({ operation: 'filter', movies: response.data, goldenPalmCount });
      toast.success('Фильмы успешно получены');
    } catch (error) {
      toast.error('Ошибка получения фильмов: ' + error.message);
    } finally {
      setLoading(false);
    }
  };

  const handleRedistributeOscars = async (e) => {
    e.preventDefault();
    const fromGenre = e.target.fromGenre.value;
    const toGenre = e.target.toGenre.value;
    
    if (!fromGenre || !toGenre) {
      toast.error('Пожалуйста, выберите оба жанра');
      return;
    }

    if (fromGenre === toGenre) {
      toast.error('Исходный и целевой жанры должны отличаться');
      return;
    }

    if (window.confirm(`Вы уверены, что хотите перераспределить Оскары с ${fromGenre} на ${toGenre}?`)) {
      try {
        setLoading(true);
        await axios.post(`${API_BASE_URL}/movies/redistribute-oscars?fromGenre=${fromGenre}&toGenre=${toGenre}`);
        setResults({ operation: 'redistribute', fromGenre, toGenre });
        toast.success('Оскары успешно перераспределены');
      } catch (error) {
        toast.error('Ошибка перераспределения Оскаров: ' + error.message);
      } finally {
          setLoading(false);
        }
      }
  };

  const handleAddOscarsToLongMovies = async (e) => {
    e.preventDefault();
    const minLength = e.target.minLength.value;
    const oscarsToAdd = e.target.oscarsToAdd.value;
    
    if (!minLength || !oscarsToAdd) {
      toast.error('Пожалуйста, введите минимальную длительность и количество Оскаров для добавления');
      return;
    }

    if (window.confirm(`Вы уверены, что хотите добавить ${oscarsToAdd} Оскаров к фильмам длиннее ${minLength} минут?`)) {
      try {
        setLoading(true);
        await axios.post(`${API_BASE_URL}/movies/add-oscars-to-long-movies?minLength=${minLength}&oscarsToAdd=${oscarsToAdd}`);
        setResults({ operation: 'addOscars', minLength, oscarsToAdd });
        toast.success('Оскары успешно добавлены');
      } catch (error) {
        toast.error('Ошибка добавления Оскаров: ' + error.message);
      } finally {
        setLoading(false);
      }
    }
  };

  const handleGetOscarsSummary = async () => {
    try {
      setLoading(true);
      const response = await axios.get(`${API_BASE_URL}/movies/oscars-summary`);
      setResults({ operation: 'oscarsSummary', data: response.data });
      toast.success('Информация об Оскарах загружена');
    } catch (error) {
      toast.error('Ошибка загрузки информации об Оскарах: ' + error.message);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="special-operations">
      <div className="operations-header">
        <h1>Специальные операции</h1>
        <p>Выполнение специальных операций с фильмами</p>
        <button 
          onClick={handleGetOscarsSummary} 
          disabled={loading}
          className="btn btn-info"
        >
          {loading ? 'Загрузка...' : 'Проверить состояние Оскаров'}
        </button>
      </div>

      <div className="operations-grid">
        <div className="operation-card">
          <h3>Удаление фильмов по количеству Золотых пальм</h3>
          <p>Удалить любой фильм с указанным количеством Золотых пальм</p>
          <form onSubmit={handleDeleteByGoldenPalm}>
            <div className="form-group">
              <label htmlFor="deleteGoldenPalm">Количество Золотых пальм</label>
              <input
                type="number"
                id="deleteGoldenPalm"
                name="goldenPalmCount"
                min="0"
                required
                className="form-input"
              />
            </div>
            <button type="submit" disabled={loading} className="btn btn-danger">
              {loading ? 'Удаление...' : 'Удалить фильмы'}
            </button>
          </form>
        </div>

        <div className="operation-card">
          <h3>Вычисление суммы Золотых пальм</h3>
          <p>Вычислить общую сумму всех Золотых пальм</p>
          <button onClick={handleGetSum} disabled={loading} className="btn btn-primary">
            {loading ? 'Вычисление...' : 'Вычислить сумму'}
          </button>
        </div>

        <div className="operation-card">
          <h3>Получение фильмов по количеству Золотых пальм</h3>
          <p>Получить фильмы с количеством Золотых пальм больше указанного значения</p>
          <form onSubmit={handleGetMoviesByGoldenPalm}>
            <div className="form-group">
              <label htmlFor="filterGoldenPalm">Количество Золотых пальм</label>
              <input
                type="number"
                id="filterGoldenPalm"
                name="goldenPalmCount"
                min="0"
                required
                className="form-input"
              />
            </div>
            <button type="submit" disabled={loading} className="btn btn-primary">
              {loading ? 'Поиск...' : 'Получить фильмы'}
            </button>
          </form>
        </div>

        <div className="operation-card">
          <h3>Перераспределение Оскаров</h3>
          <p>Равномерно перераспределить Оскары с одного жанра на другой</p>
          <form onSubmit={handleRedistributeOscars}>
            <div className="form-row">
              <div className="form-group">
                <label htmlFor="fromGenre">Исходный жанр</label>
                <select id="fromGenre" name="fromGenre" required className="form-input">
                  <option value="">Выберите жанр</option>
                  {availableGenres.map(genre => (
                    <option key={genre} value={genre}>{genre}</option>
                  ))}
                </select>
              </div>
              <div className="form-group">
                <label htmlFor="toGenre">Целевой жанр</label>
                <select id="toGenre" name="toGenre" required className="form-input">
                  <option value="">Выберите жанр</option>
                  {availableGenres.map(genre => (
                    <option key={genre} value={genre}>{genre}</option>
                  ))}
                </select>
              </div>
            </div>
            <button type="submit" disabled={loading} className="btn btn-warning">
              {loading ? 'Перераспределение...' : 'Перераспределить Оскары'}
            </button>
          </form>
        </div>

        <div className="operation-card">
          <h3>Добавление Оскаров длинным фильмам</h3>
          <p>Добавить указанное количество Оскаров к фильмам длиннее указанной продолжительности</p>
          <form onSubmit={handleAddOscarsToLongMovies}>
            <div className="form-row">
              <div className="form-group">
                <label htmlFor="minLength">Минимальная длительность (минуты)</label>
                <input
                  type="number"
                  id="minLength"
                  name="minLength"
                  min="0"
                  required
                  className="form-input"
                />
              </div>
              <div className="form-group">
                <label htmlFor="oscarsToAdd">Оскары для добавления</label>
                <input
                  type="number"
                  id="oscarsToAdd"
                  name="oscarsToAdd"
                  min="1"
                  required
                  className="form-input"
                />
              </div>
            </div>
            <button type="submit" disabled={loading} className="btn btn-success">
              {loading ? 'Добавление...' : 'Добавить Оскары'}
            </button>
          </form>
        </div>
      </div>

      {results && (
        <div className="results-section">
          <h3>Результаты операций</h3>
          <div className="results-content">
            {results.operation === 'delete' && (
              <div className="result-item">
                <p>Фильмы с {results.goldenPalmCount} Золотыми пальмами были удалены.</p>
              </div>
            )}
            {results.operation === 'sum' && (
              <div className="result-item">
                <p>Общая сумма Золотых пальм: <strong>{results.sum}</strong></p>
              </div>
            )}
            {results.operation === 'filter' && (
              <div className="result-item">
                <p>Найдено {results.movies.length} фильмов с количеством Золотых пальм больше {results.goldenPalmCount}:</p>
                <ul>
                  {results.movies.map(movie => (
                    <li key={movie.id}>{movie.name} - {movie.goldenPalmCount} Золотых пальм</li>
                  ))}
                </ul>
              </div>
            )}
            {results.operation === 'redistribute' && (
              <div className="result-item">
                <p>Оскары были перераспределены с <strong>{results.fromGenre}</strong> на <strong>{results.toGenre}</strong>.</p>
              </div>
            )}
            {results.operation === 'addOscars' && (
              <div className="result-item">
                <p>Добавлено <strong>{results.oscarsToAdd}</strong> Оскаров к фильмам длиннее <strong>{results.minLength}</strong> минут.</p>
              </div>
            )}
            {results.operation === 'oscarsSummary' && (
              <div className="result-item">
                <h4>Состояние Оскаров по жанрам:</h4>
                <ul>
                  {Object.entries(results.data.genreOscars).map(([genre, oscars]) => (
                    <li key={genre}><strong>{genre}</strong>: {oscars} Оскаров</li>
                  ))}
                </ul>
                <p><strong>Всего Оскаров:</strong> {results.data.totalOscars}</p>
                <p><strong>Всего фильмов:</strong> {results.data.totalMovies}</p>
              </div>
            )}
          </div>
        </div>
      )}
    </div>
  );
};

export default SpecialOperations;
