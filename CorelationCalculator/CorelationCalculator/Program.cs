using System;
using System.Collections.Generic;
using System.IO;
using System.Linq;
using System.Text;
using System.Text.RegularExpressions;
using System.Threading.Tasks;
using CsvHelper;
using MathNet.Numerics.Statistics;

namespace CorelationCalculator
{
    class Program
    {
        static void Main(string[] args)
        {
            var empPath = Environment.CurrentDirectory + "\\employment\\total_in_selective_area\\part-00000";
            var housePath = Environment.CurrentDirectory + "\\housing\\yearlyTotals\\part-00000";
            var popPath = Environment.CurrentDirectory + "\\population\\yearlyTotals\\part-00000";
            var popQuntilesPath = Environment.CurrentDirectory + "\\population\\cumulativeDiff\\part-00000";

            var empLines = File.ReadAllLines(empPath);
            var houseLines = File.ReadAllLines(housePath);
            var popLines = File.ReadAllLines(popPath);
            var popQuintileLines = File.ReadAllLines(popQuntilesPath);

            var numOnly = new Regex("[^0-9.,]");
            var empLinesScrubbed = empLines.Select(line => numOnly.Replace(line, ""));
            var houseLinesScrubbed = houseLines.Select(line => numOnly.Replace(line, ""));
            var popLinesScrubbed = popLines.Select(line => numOnly.Replace(line, ""));
            var popQuintLinesScrubbed = popQuintileLines.Select(line => numOnly.Replace(line, ""));

            var metros = new Dictionary<string, string>();
            var metrosPath = Environment.CurrentDirectory + "\\CountyCrossWalk_Zillow.csv";
            var textReader = File.OpenText(metrosPath);
            var csv = new CsvReader(textReader);
            while (csv.Read())
            {
                if (!metros.ContainsKey(csv.GetField(9)))
                metros.Add(csv.GetField(9), csv.GetField(5));
            }

            var quintiles = popQuintLinesScrubbed.Select(line => line.Split(',')[1]).ToList();

            var houseData = BuildData(houseLinesScrubbed);
            var popData = BuildData(popLinesScrubbed);
            var empData = new Dictionary<string, double[]>();
            foreach (var empLine in empLinesScrubbed)
            {
                var data = empLine.Split(',');
                var key = data[0];
                var value = new List<double>();
                for (var i = 2; i < data.Length - 2; i += 2)
                {
                    value.Add(Convert.ToDouble(data[i]));
                }
                empData.Add(key, value.ToArray());
            }

            var commonMetros = new HashSet<string>(empData.Keys);
            commonMetros.IntersectWith(popData.Keys);
            commonMetros.IntersectWith(houseData.Keys);

            var corOut = "Totals_Correlation_Output.csv";
            var q1 = "Quintile1.csv";
            var q2 = "Quintile2.csv";
            var q3 = "Quintile3.csv";
            var q4 = "Quintile4.csv";
            var q5 = "Quintile5.csv";
            using (var sw = File.AppendText(corOut))
            using (var sw1 = File.AppendText(q1))
            using (var sw2 = File.AppendText(q2))
            using (var sw3 = File.AppendText(q3))
            using (var sw4 = File.AppendText(q4))
            using (var sw5 = File.AppendText(q5))
            {
                sw.WriteLine("ID,Name,Housing_Populaiton,Housing_Employment,Employment_Population");
                foreach (var metro in commonMetros)
                {
                    var hpCorr = Correlation.Pearson(houseData[metro], popData[metro]);
                    var heCorr = Correlation.Pearson(houseData[metro], empData[metro]);
                    var epCorr = Correlation.Pearson(empData[metro], popData[metro]);
                    var lineOut = $"{metro},\"{metros[metro]}\",{hpCorr},{heCorr},{epCorr}";
                    sw.WriteLine(lineOut);

                    var q = quintiles.Count / 5;
                    switch (quintiles.FindIndex(x => x.Equals(metro)) / q)
                    {
                        case 0:
                            sw1.WriteLine(lineOut);
                            break;
                        case 1:
                            sw2.WriteLine(lineOut);
                            break;
                        case 2:
                            sw3.WriteLine(lineOut);
                            break;
                        case 3:
                            sw4.WriteLine(lineOut);
                            break;
                        case 4:
                            sw5.WriteLine(lineOut);
                            break;
                    }
                }
            }
        }

        private static Dictionary<string, double[]> BuildData(IEnumerable<string> dataIn)
        {
            var outData = new Dictionary<string, double[]>();
            foreach (var line in dataIn)
            {
                var data = line.Split(',');
                var key = data[1];
                var value = new List<double>();
                for (var i = 2; i < data.Length; i++)
                {
                    value.Add(Convert.ToDouble(data[i]));
                }
                outData.Add(key, value.ToArray());
            }

            return outData;
        }
    }
}
