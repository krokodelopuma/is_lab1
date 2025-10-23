import React from 'react';
import { BrowserRouter as Router, Routes, Route, Link } from 'react-router-dom';
import { ToastContainer } from 'react-toastify';
import 'react-toastify/dist/ReactToastify.css';
import './App.css';
import MovieList from './components/MovieList';
import MovieForm from './components/MovieForm';
import SpecialOperations from './components/SpecialOperations';

function App() {
  return (
    <Router>
      <div className="App">
        <nav className="navbar">
          <div className="nav-container">
            <Link to="/" className="nav-brand">
              Система управления фильмами
            </Link>
            <div className="nav-menu">
              <Link to="/" className="nav-link">Фильмы</Link>
              <Link to="/special-operations" className="nav-link">Специальные операции</Link>
            </div>
          </div>
        </nav>

        <main className="main-content">
          <Routes>
            <Route path="/" element={<MovieList />} />
            <Route path="/movie/new" element={<MovieForm />} />
            <Route path="/movie/edit/:id" element={<MovieForm />} />
            <Route path="/special-operations" element={<SpecialOperations />} />
          </Routes>
        </main>

        <ToastContainer
          position="top-right"
          autoClose={3000}
          hideProgressBar={false}
          newestOnTop={false}
          closeOnClick
          rtl={false}
          pauseOnFocusLoss
          draggable
          pauseOnHover
        />
      </div>
    </Router>
  );
}

export default App;
