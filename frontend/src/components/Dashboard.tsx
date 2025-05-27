import React, { useState } from 'react';
import apiClient from '../services/apiClient';
import styles from './Dashboard.module.css';

export const Dashboard = () => {
  const [originalUrl, setOriginalUrl] = useState('');
  const [isLoading, setIsLoading] = useState(false);
  const [result, setResult] = useState<string | null>(null);
  const [error, setError] = useState<string | null>(null);

  const handleSubmit = async (event: React.FormEvent) => {
    event.preventDefault();
    setIsLoading(true);
    setError(null);
    setResult(null);

    try {
      const response = await apiClient.post('/v1/urls', { originalUrl });
      setResult(`Link encurtado: ${response.data.shortUrl}`);
      setOriginalUrl(''); // Limpa o campo após o sucesso
    } catch (err) {
      setError('Ocorreu um erro ao encurtar a URL. Verifique o link e tente novamente.');
      console.error(err);
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div className={styles.dashboardContainer}>
      <h2>Encurtar Nova URL</h2>
      <form onSubmit={handleSubmit} className={styles.form}>
        <input
          type="text"
          className={styles.input}
          value={originalUrl}
          onChange={(e) => setOriginalUrl(e.target.value)}
          placeholder="Cole sua URL longa aqui"
          disabled={isLoading}
        />
        <button type="submit" className={styles.button} disabled={isLoading}>
          {isLoading ? 'Encurtando...' : 'Encurtar'}
        </button>
      </form>
      {error && <p className={styles.error}>{error}</p>}
      {result && <p className={styles.result}>{result}</p>}
    </div>
  );
};