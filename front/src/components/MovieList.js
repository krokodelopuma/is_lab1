import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { toast } from 'react-toastify';
import axios from 'axios';
import useWebSocket from '../hooks/useWebSocket';
import './MovieList.css';

const API_BASE_URL = 'http://localhost:8080/back-1.0-SNAPSHOT/api';

const MovieList = () => {
  const [movies, setMovies] = useState([]);
  const [loading, setLoading] = useState(true);
  const [currentPage, setCurrentPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [searchTerm, setSearchTerm] = useState('');
  const [genreFilter, setGenreFilter] = useState('');
  const [directorFilter, setDirectorFilter] = useState('');
  const [minOscars, setMinOscars] = useState('');
  const [maxOscars, setMaxOscars] = useState('');
  const [sortField, setSortField] = useState('id');
  const [sortDirection, setSortDirection] = useState('asc');

  const pageSize = 10;

  // WebSocket connection
  const { isConnected, lastMessage, connectionError } = useWebSocket('ws://localhost:8080/back-1.0-SNAPSHOT/websocket/movies');

  useEffect(() => {
    fetchMovies();
  }, [currentPage, searchTerm, genreFilter, directorFilter, minOscars, maxOscars, sortField, sortDirection]);

  // Listen for WebSocket updates
  useEffect(() => {
    if (lastMessage && lastMessage.type === 'update') {
      console.log('Received update notification, refreshing movies...');
      fetchMovies();
    }
  }, [lastMessage]);

  const fetchMovies = async () => {
    try {
      setLoading(true);
      const params = new URLSearchParams({
        page: currentPage,
        size: pageSize,
        ...(searchTerm && { search: searchTerm }),
        ...(genreFilter && { genre: genreFilter }),
        ...(directorFilter && { director: directorFilter }),
        ...(minOscars && minOscars !== '' && { minOscars: minOscars }),
        ...(maxOscars && maxOscars !== '' && { maxOscars: maxOscars }),
        ...(sortField && { sort: sortField }),
        ...(sortDirection && { order: sortDirection })
      });

      console.log('Fetching movies with params:', params.toString());
      const response = await axios.get(`${API_BASE_URL}/movies?${params}`);
      console.log('Received movies:', response.data.movies.length);
      setMovies(response.data.movies);
      setTotalPages(Math.ceil(response.data.totalCount / pageSize));
    } catch (error) {
      toast.error('Ошибка загрузки фильмов: ' + error.message);
    } finally {
      setLoading(false);
    }
  };

  const handleDelete = async (id) => {
    if (window.confirm('Вы уверены, что хотите удалить этот фильм?')) {
      try {
        await axios.delete(`${API_BASE_URL}/movies/${id}`);
        toast.success('Фильм успешно удален');
        fetchMovies();
      } catch (error) {
        toast.error('Ошибка удаления фильма: ' + error.message);
      }
    }
  };

  const handleSort = (field) => {
    if (sortField === field) {
      setSortDirection(sortDirection === 'asc' ? 'desc' : 'asc');
    } else {
      setSortField(field);
      setSortDirection('asc');
    }
  };

  const handleSearch = (e) => {
    e.preventDefault();
    setCurrentPage(0);
    fetchMovies();
  };

  const clearFilters = () => {
    setSearchTerm('');
    setGenreFilter('');
    setDirectorFilter('');
    setMinOscars('');
    setMaxOscars('');
    setCurrentPage(0);
  };

  const formatDate = (dateString) => {
    return new Date(dateString).toLocaleDateString();
  };

  const formatDateTime = (dateTimeString) => {
    return new Date(dateTimeString).toLocaleString();
  };

  if (loading) {
    return <div className="loading">Загрузка фильмов...</div>;
  }

  return (
    <div className="movie-list">
      <div className="movie-list-header">
        <div>
          <h1>Фильмы</h1>
          <div className="connection-status">
            <span className={`status-indicator ${isConnected ? 'connected' : 'disconnected'}`}>
              {isConnected ? '🟢 Подключено' : '🔴 Отключено'}
            </span>
            {connectionError && (
              <div className="connection-error">
                <small>{connectionError}</small>
              </div>
            )}
          </div>
        </div>
        <Link to="/movie/new" className="btn btn-primary">
          Добавить фильм
        </Link>
      </div>

      <div className="filters">
        <div className="filters-grid">
          <div className="filter-group">
            <label>Поиск по названию, жанру, режиссеру:</label>
            <input
              type="text"
              placeholder="Введите текст для поиска..."
              value={searchTerm}
              onChange={(e) => setSearchTerm(e.target.value)}
              className="search-input"
            />
          </div>
          
          <div className="filter-group">
            <label>Жанр:</label>
            <select
              value={genreFilter}
              onChange={(e) => setGenreFilter(e.target.value)}
              className="filter-select"
            >
              <option value="">Все жанры</option>
              <option value="DRAMA">Драма</option>
              <option value="ADVENTURE">Приключения</option>
              <option value="FANTASY">Фэнтези</option>
            </select>
          </div>
          
          <div className="filter-group">
            <label>Режиссер:</label>
            <input
              type="text"
              placeholder="Имя режиссера..."
              value={directorFilter}
              onChange={(e) => setDirectorFilter(e.target.value)}
              className="filter-input"
            />
          </div>
          
          <div className="filter-group">
            <label>Оскары (от):</label>
            <input
              type="number"
              placeholder="0"
              value={minOscars}
              onChange={(e) => setMinOscars(e.target.value)}
              className="filter-input"
              min="0"
            />
          </div>
          
          <div className="filter-group">
            <label>Оскары (до):</label>
            <input
              type="number"
              placeholder="∞"
              value={maxOscars}
              onChange={(e) => setMaxOscars(e.target.value)}
              className="filter-input"
              min="0"
            />
          </div>
          
          <div className="filter-group">
            <button onClick={clearFilters} className="btn btn-secondary">
              Сбросить фильтры
            </button>
          </div>
        </div>
      </div>

      <div className="table-container">
        <table className="movies-table">
          <thead>
            <tr>
              <th onClick={() => handleSort('id')}>
                ID {sortField === 'id' && (sortDirection === 'asc' ? '↑' : '↓')}
              </th>
              <th onClick={() => handleSort('name')}>
                Название {sortField === 'name' && (sortDirection === 'asc' ? '↑' : '↓')}
              </th>
              <th>Координаты</th>
              <th onClick={() => handleSort('creationDate')}>
                Дата создания {sortField === 'creationDate' && (sortDirection === 'asc' ? '↑' : '↓')}
              </th>
              <th onClick={() => handleSort('oscarsCount')}>
                Оскары {sortField === 'oscarsCount' && (sortDirection === 'asc' ? '↑' : '↓')}
              </th>
              <th onClick={() => handleSort('budget')}>
                Бюджет {sortField === 'budget' && (sortDirection === 'asc' ? '↑' : '↓')}
              </th>
              <th onClick={() => handleSort('totalBoxOffice')}>
                Кассовые сборы {sortField === 'totalBoxOffice' && (sortDirection === 'asc' ? '↑' : '↓')}
              </th>
              <th onClick={() => handleSort('mpaaRating')}>
                MPAA {sortField === 'mpaaRating' && (sortDirection === 'asc' ? '↑' : '↓')}
              </th>
              <th>Режиссер</th>
              <th>Сценарист</th>
              <th>Оператор</th>
              <th onClick={() => handleSort('length')}>
                Длительность {sortField === 'length' && (sortDirection === 'asc' ? '↑' : '↓')}
              </th>
              <th onClick={() => handleSort('goldenPalmCount')}>
                Золотые пальмы {sortField === 'goldenPalmCount' && (sortDirection === 'asc' ? '↑' : '↓')}
              </th>
              <th onClick={() => handleSort('genre')}>
                Жанр {sortField === 'genre' && (sortDirection === 'asc' ? '↑' : '↓')}
              </th>
              <th>Действия</th>
            </tr>
          </thead>
          <tbody>
            {movies.map((movie) => (
              <tr key={movie.id}>
                <td>{movie.id}</td>
                <td>{movie.name}</td>
                <td>
                  {movie.coordinates ? 
                    `X: ${movie.coordinates.x}, Y: ${movie.coordinates.y}` : 
                    'N/A'
                  }
                </td>
                <td>{movie.creationDate ? formatDateTime(movie.creationDate) : 'N/A'}</td>
                <td>{movie.oscarsCount}</td>
                <td>{movie.budget ? `$${movie.budget.toLocaleString()}` : 'N/A'}</td>
                <td>${movie.totalBoxOffice.toLocaleString()}</td>
                <td>{movie.mpaaRating || 'N/A'}</td>
                <td>{movie.director ? movie.director.name : 'N/A'}</td>
                <td>{movie.screenwriter ? movie.screenwriter.name : 'N/A'}</td>
                <td>{movie.operator ? movie.operator.name : 'N/A'}</td>
                <td>{movie.length} min</td>
                <td>{movie.goldenPalmCount || 'N/A'}</td>
                <td>{movie.genre}</td>
                <td>
                  <div className="action-buttons">
                    <Link to={`/movie/edit/${movie.id}`} className="btn btn-sm btn-primary">
                      Редактировать
                    </Link>
                    <button
                      onClick={() => handleDelete(movie.id)}
                      className="btn btn-sm btn-danger"
                    >
                      Удалить
                    </button>
                  </div>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>

      <div className="pagination">
        <button
          onClick={() => setCurrentPage(Math.max(0, currentPage - 1))}
          disabled={currentPage === 0}
          className="btn btn-secondary"
        >
          Назад
        </button>
        <span className="page-info">
          Страница {currentPage + 1} из {totalPages}
        </span>
        <button
          onClick={() => setCurrentPage(Math.min(totalPages - 1, currentPage + 1))}
          disabled={currentPage >= totalPages - 1}
          className="btn btn-secondary"
        >
          Вперед
        </button>
      </div>
    </div>
  );
};

export default MovieList;
