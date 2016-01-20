using Microsoft.VisualStudio.TestTools.UnitTesting;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using EsgynDB.Data;

namespace AdoSamples
{
    [TestClass]
    public class DBParameterTest
    {
        private string ConnectionString = "server=10.10.10.173:23400;user=zz;password=zz;schema=ado";
        [TestMethod]
        public void findByValue()
        {
            EsgynDBParameter p1 = new EsgynDBParameter("param-1","abc");
            EsgynDBParameter p2 = new EsgynDBParameter("param-2", "def");
            EsgynDBParameter p3 = new EsgynDBParameter("param-3", "ghi");
            EsgynDBParameter p4 = new EsgynDBParameter("param-4", "jkl");
            EsgynDBParameter p5 = new EsgynDBParameter("param-5", "mno");
            EsgynDBConnection conn = new EsgynDBConnection();
            conn.ConnectionString = ConnectionString;
            conn.Open();
            EsgynDBCommand cmd = conn.CreateCommand();
            EsgynDBParameterCollection list = cmd.Parameters;
            list.Add(p1);
            list.Add(p2);
            list.Add(p3);
            list.Add(p4);
            list.Add(p5);

            EsgynDBParameter test = new EsgynDBParameter("param-test", "jkl");
            Assert.IsTrue(list.Contains(test));
            Assert.IsFalse(list.Contains("aaa"));
        }
    }
}
