// LoginForm.tsx

import React, { useState } from 'react';
// 1. Importamos nosso arquivo de estilos.
import styles from './LoginForm.module.css';

export const LoginForm = () => {
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');

  const handleSubmit = (event: React.FormEvent) => {
    event.preventDefault();
    console.log('Username:', username);
    console.log('Password:', password);
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
          />
        </div>
        <div className={styles.formGroup}>
          <label className={styles.label}>Senha:</label>
          <input
            type="password"
            className={styles.input}
            value={password}
            onChange={(e) => setPassword(e.target.value)}
          />
        </div>
        <button type="submit" className={styles.button}>Entrar</button>
      </form>
    </div>
  );
};