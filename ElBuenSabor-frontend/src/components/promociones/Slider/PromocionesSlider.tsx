import React from 'react';
import { Swiper, SwiperSlide } from 'swiper/react';
import { Navigation, Pagination, Autoplay } from 'swiper/modules';
import PromotionCard from '../Card/PromotionCard';
import type { PromocionResponse } from '../../../types/types';
import 'swiper/swiper-bundle.css';
import './PromocionesSlider.sass';

interface PromocionesSliderProps {
  promociones: PromocionResponse[];
}

const PromocionesSlider: React.FC<PromocionesSliderProps> = ({ promociones }) => {
  return (
    <Swiper
      modules={[Navigation, Pagination, Autoplay]}
      spaceBetween={30}
      slidesPerView={1}
      navigation
      pagination={{ clickable: true }}
      loop={true}
      autoplay={{
        delay: 3500,
        disableOnInteraction: false,
      }}
      breakpoints={{
        768: {
          slidesPerView: 2,
          spaceBetween: 40,
        },
        1024: {
          slidesPerView: 2,
          spaceBetween: 50,
        },
      }}
      className="promociones-slider"
    >
      {promociones.map(promo => (
        <SwiperSlide key={promo.id}>
          <PromotionCard promocion={promo} />
        </SwiperSlide>
      ))}
    </Swiper>
  );
};

export default PromocionesSlider;