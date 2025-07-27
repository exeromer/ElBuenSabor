import Titulo from '../../../components/utils/Titulo/Titulo'
import Contenedor from '../../../components/utils/Contenedor/Contenedor'
import './Nosotros.sass'


export const Nosotros = () => {
    return (
        <Contenedor>
            <Titulo texto='Sobre nosotros' nivel='titulo' />
            <p>
                En el corazón de Mendoza, nace "El Buen Sabor" con una pasión: llevar hasta tu mesa el gusto auténtico de la comida casera. Somos un servicio de delivery que combina recetas tradicionales y los ingredientes más frescos con la comodidad que la vida moderna exige.
            </p>
            <p>
                Nuestra misión es simple: ofrecerte un menú delicioso y variado, desde pizzas y hamburguesas hasta lomos y minutas, todo preparado al momento y con el cuidado que nos caracteriza. A través de nuestra plataforma web, diseñada para funcionar perfectamente en cualquier dispositivo, puedes explorar nuestros platos, realizar tu pedido y recibirlo en la puerta de tu casa o retirarlo en nuestro local.
            </p>
            <p>
                Creemos que disfrutar de una buena comida debe ser una experiencia fácil y placentera. Por eso, hemos desarrollado un sistema inteligente que no solo te permite ordenar con unos pocos clics, sino que también gestiona nuestro stock en tiempo real para asegurarte que siempre recibas lo que más te gusta.
            </p>
            <p>
                <strong>¡Bienvenido a la familia de El Buen Sabor!</strong>
            </p>
        </Contenedor>
    )
}

export default Nosotros