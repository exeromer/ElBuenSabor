// import './Slider.sass'
// import { useEffect, useState } from 'react'
// import Carousel from 'react-bootstrap/Carousel'
// import type { Instrumento } from '../../types/types'
// import { getInstrumentos } from '../../services/FuncionesApi'
// import { FontAwesomeIcon } from '@fortawesome/react-fontawesome'
// import { faChevronLeft, faChevronRight } from '@fortawesome/free-solid-svg-icons'
// import Titulo from '../../utils/Titulo/Titulo'
// import Contenedor from '../../utils/Contenedor/Contenedor'

// const Slider = () => {
//   const [instrumentos, setInstrumentos] = useState<Instrumento[]>([]);

//   useEffect(() => {
//     const fetchInstrumentos = async () => {
//       try {
//         const data = await getInstrumentos();
//         setInstrumentos(data);
//       } catch (error) {
//         console.error('Error al obtener instrumentos:', error);
//       }
//     };

//     fetchInstrumentos();
//   }, []);

//   const instrumentosParaSlider = instrumentos.slice(0, 3);

//   return (
//     <Contenedor>
//       <Titulo texto='Los mejores instrumentos' nivel='titulo' />
//       <Carousel
//         className='slider'
//         prevIcon={<FontAwesomeIcon icon={faChevronLeft} className="custom-arrow" />}
//         nextIcon={<FontAwesomeIcon icon={faChevronRight} className="custom-arrow" />}
//       >
//         {instrumentosParaSlider.map((instrumento) => (
//           <Carousel.Item key={instrumento.id}>
//             <div className='slider-image-contenedor'>
//               <img
//                 className="d-block w-100"
//                 src={`./images/${instrumento.imagen}`}
//                 alt={instrumento.instrumento}
//                 style={{ objectFit: 'cover' }}
//               />
//             </div>
//             <Carousel.Caption>
//               <h3>{instrumento.instrumento}</h3>
//             </Carousel.Caption>
//           </Carousel.Item>
//         ))}
//       </Carousel>
//     </Contenedor>
//   );
// };

// export default Slider;
