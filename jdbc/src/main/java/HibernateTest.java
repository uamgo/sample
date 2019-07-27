import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.AnnotationConfiguration;
import org.hibernate.cfg.Configuration;
import org.hibernate.transform.Transformers;

import java.io.File;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class HibernateTest {

  private static org.hibernate.classic.Session session;

  public static List<Object> findPageBySqlQuery(int first, int pageSize, String hql, Map map,
      Boolean isUsingCache) {
    List<Object> result = null;
    try {
      Query query = session.createSQLQuery(hql);
      System.out.println("hql================" + hql);

      //      Iterator it = map.keySet().iterator();
      //      while (it.hasNext()) {
      //        Object key = it.next();
      //        Object value = map.get(key);
      //        if (value != null && !value.toString().equals("") && !(key.toString().equals("start")) && (
      //            !key.toString().equals("limit") && (!key.toString().equals("sortString")))) {
      //          query.setParameter(key.toString(), value);
      //        }
      //      }

      System.out.println("first================" + (first - 1));
      System.out.println("pageSize=============" + pageSize);
      query.setFirstResult((first - 1));
      query.setMaxResults(pageSize);

      query.setResultTransformer(Transformers.ALIAS_TO_ENTITY_MAP);//

      result = query.list();
      System.err.println("result.size()=============" + result.size());
      if(result.size() == 0){
        System.exit(0);
      }
    } catch (RuntimeException re) {
      re.printStackTrace();
    }
    return result;
  }

  public static void main(String[] args) {
    Configuration config = new AnnotationConfiguration().configure("hibernate.cfg.xml");
    SessionFactory sessionFactory = config.buildSessionFactory();
    session = sessionFactory.openSession();
    String hql = "select * from Student";
    for (int i = 0; i < 100; i++) {
      System.out.println(i + " ---------------------------");
      findPageBySqlQuery(i * 20 + 1, 20, hql, null, false);
    }
  }
}
