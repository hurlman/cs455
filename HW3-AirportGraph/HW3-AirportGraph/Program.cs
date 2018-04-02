using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace HW3_AirportGraph
{
    class Program
    {
        static void Main(string[] args)
        {
            var path = args[0];
            var lines = System.IO.File.ReadLines(path);
            var arpts = new Dictionary<string, int[]>();

            foreach (var s in lines)
            {
                var data = s.Split(',');
                if (arpts.ContainsKey(data[1]))
                {
                    arpts[data[1]][int.Parse(data[0]) - 1987] = int.Parse(data[2]);
                }
                else
                {
                    arpts.Add(data[1], new int[22]);
                    arpts[data[1]][int.Parse(data[0]) - 1987] = int.Parse(data[2]);
                }
            }

            var output = new string[23];
            output[0] = "," + String.Join(",", arpts.Keys.ToArray());
            for (int i = 0; i < 22; i++)
            {
                var sb = new StringBuilder(i + 1987 + ",");

                foreach(var v in arpts.Values)
                {
                    var o = v[i] == 0 ? "" : v[i].ToString();
                    sb.Append(o + ',');
                }

                output[i + 1] = sb.ToString();
            }

            System.IO.File.WriteAllLines("output.csv", output);
        }
    }
}
