import { Swiper, SwiperSlide } from "swiper/react";
import { Autoplay, Pagination } from "swiper/modules";
import "swiper/css";
import "swiper/css/pagination";

export default function ScreenshotCarousel({ slides }) {
  return (
    <div className="glass-panel p-5 md:p-7">
      <Swiper
        modules={[Autoplay, Pagination]}
        spaceBetween={20}
        slidesPerView={1.1}
        centeredSlides
        loop
        autoplay={{ delay: 3200 }}
        pagination={{ clickable: true }}
        breakpoints={{
          768: { slidesPerView: 2.1 },
          1024: { slidesPerView: 3 }
        }}
      >
        {slides.map((slide) => (
          <SwiperSlide key={slide.title}>
            <figure className="group overflow-hidden rounded-2xl border border-white/10 bg-black/30 p-4 transition hover:border-accent-300/60">
              <div className="h-[420px] overflow-hidden rounded-xl">
                <img
                  src={slide.image}
                  alt={slide.title}
                  loading="lazy"
                  className="h-[470px] w-full -translate-y-6 object-cover object-top transition duration-500 group-hover:scale-[1.03]"
                />
              </div>
              <figcaption className="mt-4">
                <h4 className="font-heading text-lg font-semibold text-white">{slide.title}</h4>
                <p className="text-sm text-slate-300">{slide.subtitle}</p>
              </figcaption>
            </figure>
          </SwiperSlide>
        ))}
      </Swiper>
    </div>
  );
}
