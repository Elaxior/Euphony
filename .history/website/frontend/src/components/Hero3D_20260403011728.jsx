import { Canvas, useFrame } from "@react-three/fiber";
import { Environment, Float } from "@react-three/drei";
import { useRef } from "react";

function Orb({ color, position, scale = 1, speed = 0.4 }) {
  const ref = useRef(null);

  useFrame((state) => {
    if (!ref.current) return;
    ref.current.rotation.x = state.clock.elapsedTime * speed;
    ref.current.rotation.y = state.clock.elapsedTime * speed * 1.3;
  });

  return (
    <Float speed={1.4} rotationIntensity={0.6} floatIntensity={1.1}>
      <mesh ref={ref} position={position} scale={scale}>
        <icosahedronGeometry args={[1, 1]} />
        <meshStandardMaterial color={color} metalness={0.4} roughness={0.15} />
      </mesh>
    </Float>
  );
}

function Ring({ position }) {
  const ref = useRef(null);

  useFrame((state) => {
    if (!ref.current) return;
    ref.current.rotation.z = state.clock.elapsedTime * 0.25;
    ref.current.rotation.y = state.clock.elapsedTime * 0.15;
  });

  return (
    <mesh ref={ref} position={position}>
      <torusGeometry args={[1.7, 0.08, 24, 180]} />
      <meshStandardMaterial color="#9e86ff" emissive="#4a35a8" emissiveIntensity={0.5} />
    </mesh>
  );
}

export default function Hero3D() {
  return (
    <div className="glass-panel h-[360px] overflow-hidden md:h-[420px]">
      <Canvas camera={{ position: [0, 0, 6.3], fov: 42 }}>
        <color attach="background" args={["#0b0b15"]} />
        <ambientLight intensity={0.8} />
        <directionalLight position={[4, 4, 5]} intensity={1.3} color="#b9a8ff" />
        <pointLight position={[-4, -2, 2]} intensity={1} color="#67d8ff" />

        <Ring position={[0, 0, 0]} />
        <Orb color="#7c5cff" position={[-1.7, 0.8, 0.5]} scale={0.95} speed={0.5} />
        <Orb color="#67d8ff" position={[1.8, -0.8, 0]} scale={0.75} speed={0.75} />
        <Orb color="#d3c8ff" position={[0.5, 1.5, -0.8]} scale={0.52} speed={0.92} />

        <Environment preset="city" />
      </Canvas>
    </div>
  );
}
