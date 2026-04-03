import { Swiper, SwiperSlide } from "swiper/react";
import { Autoplay, Pagination } from "swiper/modules";
import "swiper/css";
import "swiper/css/pagination";

export default function ScreenshotCarousel({ slides }) {
  return (
    <div className="glass-panel p-3 sm:p-4 md:p-7">
      <Swiper
        modules={[Autoplay, Pagination]}
        spaceBetween={12}
        slidesPerView={1}
        centeredSlides={false}
        loop
        autoplay={{ delay: 3200 }}
        pagination={{ clickable: true }}
        breakpoints={{
          640: { slidesPerView: 1.15, centeredSlides: true, spaceBetween: 14 },
          768: { slidesPerView: 1.6, centeredSlides: true, spaceBetween: 18 },
          1024: { slidesPerView: 3 }
        }}
      >
        {slides.map((slide) => (
          <SwiperSlide key={slide.title}>
            <figure className="group rounded-2xl border border-white/10 bg-black/30 p-3 transition hover:border-accent-300/60 sm:p-4">
              <img
                src={slide.image}
                alt={slide.title}
                loading="lazy"
                className="mx-auto h-auto max-w-full rounded-xl object-contain"
              />
              <figcaption className="mt-3 sm:mt-4">
                <h4 className="font-heading text-base font-semibold text-white sm:text-lg">{slide.title}</h4>
                <p className="text-xs text-slate-300 sm:text-sm">{slide.subtitle}</p>
              </figcaption>
            </figure>
          </SwiperSlide>
        ))}
      </Swiper>
    </div>
  );
}
