// LoginForm.tsx

import React, { useState } from 'react';
import styles from './LoginForm.module.css';
import axios from 'axios';

export const LoginForm = () => {
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');

  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const handleSubmit = async (event: React.FormEvent) => {
    event.preventDefault();
    setIsLoading(true);
    setError(null);

    try {
      const response = await axios.post('/api/v1/auth/login', {
        username: username,
        password: password,
      });

      const token = response.data.token;
      console.log('Login bem-sucedido! Token recebido:', token);
      localStorage.setItem('authToken', token);

      alert('Login realizado com sucesso!');

    } catch (err) {
      console.error('Erro no login:', err);
      setError('Usuário ou senha inválidos. Por favor, tente novamente.');
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div className={styles.formContainer}>
      <h2>Login</h2>
      <form onSubmit={handleSubmit}>
        <div className={styles.formGroup}>
          <label className={styles.label}>Usuário:</label>
          <input
            type="text"
            className={styles.input}
            value={username}
            onChange={(e) => setUsername(e.target.value)}
            disabled={isLoading}
          />
        </div>
        <div className={styles.formGroup}>
          <label className={styles.label}>Senha:</label>
          <input
            type="password"
            className={styles.input}
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            disabled={isLoading}
          />
        </div>

        {}
        {error && <p className={styles.error}>{error}</p>}

        {}
        <button type="submit" className={styles.button} disabled={isLoading}>
          {isLoading ? 'Entrando...' : 'Entrar'}
        </button>
      </form>
    </div>
  );
};