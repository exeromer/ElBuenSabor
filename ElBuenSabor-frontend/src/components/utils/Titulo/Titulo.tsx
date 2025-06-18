import React, { type JSX } from 'react';
import './Titulo.sass'; 

type TitleLevel = 'titulo' | 'subtitulo' | 'encabezado'; // Definimos los 3 niveles semánticos

type Props = {
  texto: string;
  nivel: TitleLevel;
};

const Titulo: React.FC<Props> = ({ texto, nivel }) => {
  let Tag: keyof JSX.IntrinsicElements;
  let classNameToApply: string;

  switch (nivel) {
    case 'titulo':
      Tag = 'h1';
      classNameToApply = 'titulo-principal'; 
      break;
    case 'subtitulo':
      Tag = 'h2';
      classNameToApply = 'titulo-seccion'; 
      break;
    case 'encabezado': 
      Tag = 'h3';
      classNameToApply = 'titulo-subseccion'; 
      break;
    default:
      Tag = 'h2';
      classNameToApply = 'titulo-seccion';
  }

  return (
    <Tag className={classNameToApply}>{texto}</Tag>
  );
};

export default Titulo;