using System;
using System.IO;
using System.Text;

// Deterministic procedural Foley for the pixel-art introduction. No external samples or tools required.
public static class IntroEffectsSynth
{
    const int Rate = 44100;
    static double Sin(double phase) { return Math.Sin(phase * Math.PI * 2); }
    static double Hit(double t, double start, double decay, double frequency)
    {
        double age = t - start;
        return age < 0 ? 0 : Math.Exp(-age / decay) * (1 - Math.Exp(-age / .0015)) * Sin(age * frequency);
    }
    public static float[] Create(string kind, double seconds, int seed)
    {
        var random = new Random(seed);
        var samples = new float[(int)Math.Round(seconds * Rate)];
        double low = 0, medium = 0, crackle = 0, phase = 0;
        var taps = new double[36];
        var pitches = new double[taps.Length];
        for (int j = 0; j < taps.Length; j++) { taps[j] = random.NextDouble() * seconds; pitches[j] = 180 + random.NextDouble() * 2400; }
        double peak = 0;
        for (int i = 0; i < samples.Length; i++)
        {
            double t = i / (double)Rate, u = t / seconds;
            double noise = random.NextDouble() * 2 - 1;
            low += .025 * (noise - low);
            medium += .24 * (noise - medium);
            crackle = crackle * .94 + (random.NextDouble() < .0008 ? noise : 0);
            double value;
            switch (kind)
            {
                case "torch": value = low * 1.5 + medium * .12 + crackle * .12; break;
                case "shell_rattle":
                    value = medium * .12;
                    for (int j = 0; j < 9; j++) value += Hit(t, j * .063, .011, 1800 + j * 133) * .6;
                    break;
                case "shell_break":
                    value = (medium * 2 + (noise - medium) * .35) * Math.Exp(-t * 13);
                    for (int j = 0; j < 14; j++) value += Hit(t, taps[j] * .65, .012, pitches[j] + 900) * .5;
                    break;
                case "dragon_chirp":
                    phase += (620 + 360 * Math.Sin(u * Math.PI * 1.4) - 190 * u) / Rate;
                    value = (Sin(phase) + .23 * Sin(phase * 2) + medium * .08) * Math.Pow(Math.Sin(Math.PI * u), 2);
                    break;
                case "inhale": value = (low * 1.8 + medium * .45) * u * u; break;
                case "fire":
                    phase += (92 - 40 * u) / Rate;
                    value = (low * 3.2 + medium * 1.3 + Sin(phase) * .18 + crackle * .2) * Math.Min(1, t / .045) * Math.Pow(1 - u, .35);
                    break;
                case "wall_crack":
                    value = medium * Math.Exp(-t * 16);
                    for (int j = 0; j < 10; j++) value += Hit(t, taps[j] * .5, .025, 400 + pitches[j] * .25) * .7;
                    break;
                case "wall_impact":
                    phase += (105 * Math.Exp(-t * 9) + 34) / Rate;
                    value = (Sin(phase) * .65 + low * 3 + medium * 1.2) * Math.Exp(-t * 7);
                    break;
                case "rubble":
                    value = medium * .12 * (1 - u);
                    for (int j = 0; j < taps.Length; j++) value += Hit(t, taps[j], .018 + .025 * (1 - u), 170 + pitches[j] * .4) * (1 - u);
                    break;
                case "guard_step":
                    value = Hit(t, 0, .038, 105) + Hit(t, .012, .018, 240) * .5 + medium * .3 * Math.Exp(-t * 26);
                    value += Hit(t, .028, .022, 1700) * .19 + Hit(t, .045, .025, 2350) * .11;
                    break;
                case "dragon_step": value = Hit(t, 0, .018, 310) + Hit(t, .004, .006, 1550) * .3; break;
                case "wind": value = low * 3 + medium * .08; break;
                default: throw new ArgumentException("Unknown effect: " + kind);
            }
            double fadeIn = Math.Min(1, t / (kind == "wind" || kind == "torch" ? .25 : .003));
            double fadeOut = Math.Min(1, (seconds - t) / (kind == "wind" || kind == "torch" ? .2 : .02));
            samples[i] = (float)(value * fadeIn * fadeOut);
            peak = Math.Max(peak, Math.Abs(samples[i]));
        }
        if (peak > 0) for (int i = 0; i < samples.Length; i++) samples[i] = (float)(samples[i] * .78 / peak);
        return samples;
    }
    public static void Mix(float[] target, float[] source, double at, double gain)
    {
        int start = (int)Math.Round(at * Rate);
        if (start < 0) throw new ArgumentException("Negative cue time");
        for (int i = 0; i < source.Length && start + i < target.Length; i++) target[start + i] += (float)(source[i] * gain);
    }
    public static float[] ReadMono(string path)
    {
        using (var reader = new BinaryReader(File.OpenRead(path)))
        {
            reader.BaseStream.Position = 12;
            while (reader.BaseStream.Position + 8 <= reader.BaseStream.Length)
            {
                string id = Encoding.ASCII.GetString(reader.ReadBytes(4)); int size = reader.ReadInt32();
                if (id == "data")
                {
                    var samples = new float[size / 2];
                    for (int i = 0; i < samples.Length; i++) samples[i] = reader.ReadInt16() / 32768f;
                    return samples;
                }
                reader.BaseStream.Position += size + size % 2;
            }
        }
        throw new InvalidDataException("Missing WAV data");
    }
    public static void Write(string path, float[] samples)
    {
        double peak = 0;
        foreach (float sample in samples) peak = Math.Max(peak, Math.Abs(sample));
        double gain = peak > .86 ? .86 / peak : 1;
        using (var writer = new BinaryWriter(File.Create(path)))
        {
            writer.Write(Encoding.ASCII.GetBytes("RIFF")); writer.Write(36 + samples.Length * 2);
            writer.Write(Encoding.ASCII.GetBytes("WAVEfmt ")); writer.Write(16);
            writer.Write((short)1); writer.Write((short)1); writer.Write(Rate); writer.Write(Rate * 2);
            writer.Write((short)2); writer.Write((short)16);
            writer.Write(Encoding.ASCII.GetBytes("data")); writer.Write(samples.Length * 2);
            for (int i = 0; i < samples.Length; i++)
            {
                double fade = Math.Min(1, (samples.Length - 1 - i) / (Rate * .015));
                writer.Write((short)Math.Round(samples[i] * gain * fade * 32767));
            }
        }
    }
}
